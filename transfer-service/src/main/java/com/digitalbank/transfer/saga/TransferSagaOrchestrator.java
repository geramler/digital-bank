package com.digitalbank.transfer.saga;

import com.digitalbank.events.TransferCommand;
import com.digitalbank.events.TransferEvent;
import com.digitalbank.events.TransferStepResult;
import com.digitalbank.events.TransferStepStatus;
import com.digitalbank.transfer.model.Transfer;
import com.digitalbank.transfer.outbox.OutboxEvent;
import com.digitalbank.transfer.outbox.OutboxEventRepository;
import com.digitalbank.transfer.repository.TransferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransferSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(TransferSagaOrchestrator.class);
    private static final String COMMAND_TOPIC = "transfer.commands";
    private static final String EVENT_TOPIC = "transfer.events";

    private final TransferRepository transferRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public TransferSagaOrchestrator(
            TransferRepository transferRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.transferRepository = transferRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Transfer initiateTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String fromAccountEmail) {
        validateTransfer(fromAccountId, toAccountId, amount);

        Transfer transfer = new Transfer(fromAccountId, toAccountId, amount, fromAccountEmail);
        transfer.setStatus(Transfer.Status.DEBIT_PENDING);
        transfer = transferRepository.save(transfer);

        saveOutboxEvent(COMMAND_TOPIC, transfer.getId(),
                TransferCommand.debit(transfer.getId(), transfer.getFromAccountId(), transfer.getAmount()));
        saveOutboxEvent(EVENT_TOPIC, transfer.getId(),
                TransferEvent.initiated(transfer.getId(), transfer.getFromAccountId(),
                        transfer.getToAccountId(), transfer.getAmount(), transfer.getFromAccountEmail()));

        log.info("Transfer initiated id={} fromAccountId={} toAccountId={} amount={}",
                transfer.getId(), fromAccountId, toAccountId, amount);
        return transfer;
    }

    @KafkaListener(topics = "transfer.step-results", groupId = "transfer-saga-orchestrator")
    @Transactional
    public void onStepResult(TransferStepResult result) {
        Transfer transfer = transferRepository.findById(result.transferId())
                .orElseThrow(() -> new IllegalStateException("Transfer not found: " + result.transferId()));

        log.info("Saga received result transferId={} commandId={} type={} status={} currentState={}",
                result.transferId(), result.commandId(), result.type(), result.status(), transfer.getStatus());

        switch (result.type()) {
            case DEBIT -> handleDebitResult(transfer, result);
            case CREDIT -> handleCreditResult(transfer, result);
            case REFUND -> handleRefundResult(transfer, result);
        }
    }

    private void handleDebitResult(Transfer transfer, TransferStepResult result) {
        if (transfer.getStatus() != Transfer.Status.DEBIT_PENDING) {
            logDuplicateOrStaleResult(transfer, result);
            return;
        }

        if (result.status() == TransferStepStatus.FAILED) {
            markFailed(transfer, result.reason());
            return;
        }

        transfer.setStatus(Transfer.Status.CREDIT_PENDING);
        transfer.setUpdatedAt(Instant.now());
        transferRepository.save(transfer);
        saveOutboxEvent(COMMAND_TOPIC, transfer.getId(),
                TransferCommand.credit(transfer.getId(), transfer.getToAccountId(), transfer.getAmount()));
    }

    private void handleCreditResult(Transfer transfer, TransferStepResult result) {
        if (transfer.getStatus() != Transfer.Status.CREDIT_PENDING) {
            logDuplicateOrStaleResult(transfer, result);
            return;
        }

        if (result.status() == TransferStepStatus.FAILED) {
            transfer.setStatus(Transfer.Status.REFUND_PENDING);
            transfer.setFailureReason("Credit failed: " + safeReason(result.reason()));
            transfer.setUpdatedAt(Instant.now());
            transferRepository.save(transfer);
            saveOutboxEvent(COMMAND_TOPIC, transfer.getId(),
                    TransferCommand.refund(transfer.getId(), transfer.getFromAccountId(), transfer.getAmount()));
            return;
        }

        transfer.setStatus(Transfer.Status.COMPLETED);
        transfer.setUpdatedAt(Instant.now());
        transferRepository.save(transfer);
        saveOutboxEvent(EVENT_TOPIC, transfer.getId(),
                TransferEvent.completed(transfer.getId(), transfer.getFromAccountId(),
                        transfer.getToAccountId(), transfer.getAmount(), transfer.getFromAccountEmail()));
    }

    private void handleRefundResult(Transfer transfer, TransferStepResult result) {
        if (transfer.getStatus() != Transfer.Status.REFUND_PENDING) {
            logDuplicateOrStaleResult(transfer, result);
            return;
        }

        if (result.status() == TransferStepStatus.FAILED) {
            transfer.setStatus(Transfer.Status.MANUAL_REVIEW);
            transfer.setFailureReason(
                    transfer.getFailureReason() + "; refund failed: " + safeReason(result.reason()));
        } else {
            transfer.setStatus(Transfer.Status.FAILED);
        }

        transfer.setUpdatedAt(Instant.now());
        transferRepository.save(transfer);
        saveOutboxEvent(EVENT_TOPIC, transfer.getId(),
                TransferEvent.failed(transfer.getId(), transfer.getFromAccountId(),
                        transfer.getToAccountId(), transfer.getAmount(),
                        transfer.getFailureReason(), transfer.getFromAccountEmail()));
    }

    private void markFailed(Transfer transfer, String reason) {
        transfer.setStatus(Transfer.Status.FAILED);
        transfer.setFailureReason(safeReason(reason));
        transfer.setUpdatedAt(Instant.now());
        transferRepository.save(transfer);
        saveOutboxEvent(EVENT_TOPIC, transfer.getId(),
                TransferEvent.failed(transfer.getId(), transfer.getFromAccountId(),
                        transfer.getToAccountId(), transfer.getAmount(),
                        transfer.getFailureReason(), transfer.getFromAccountEmail()));
    }

    private void saveOutboxEvent(String topic, Long key, Object payload) {
        outboxEventRepository.save(OutboxEvent.of(topic, key.toString(), payload, objectMapper));
    }

    private void validateTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId == null || toAccountId == null) {
            throw new IllegalArgumentException("Both account IDs are required");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts must differ");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
    }

    private void logDuplicateOrStaleResult(Transfer transfer, TransferStepResult result) {
        log.info("Ignoring duplicate or stale result commandId={} type={} for transferId={} in state={}",
                result.commandId(), result.type(), transfer.getId(), transfer.getStatus());
    }

    private String safeReason(String reason) {
        return reason == null || reason.isBlank() ? "unspecified failure" : reason;
    }
}

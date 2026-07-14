package com.digitalbank.transaction.service;

import com.digitalbank.events.TransactionCommand;
import com.digitalbank.events.TransactionCompletedEvent;
import com.digitalbank.events.TransactionResult;
import com.digitalbank.events.TransactionStatus;
import com.digitalbank.events.TransactionType;
import com.digitalbank.transaction.model.Transaction;
import com.digitalbank.transaction.outbox.OutboxEvent;
import com.digitalbank.transaction.outbox.OutboxEventRepository;
import com.digitalbank.transaction.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final String COMMAND_TOPIC = "transaction.commands";
    private static final String TX_COMPLETED_TOPIC = "transaction.completed";

    private final TransactionRepository transactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Transaction initiateTransaction(Long accountId, String type, BigDecimal amount, String accountOwnerEmail) {
        TransactionType txType = parseType(type);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("accountId is required");
        }

        Transaction transaction = transactionRepository.save(new Transaction(accountId, txType.name(), amount, accountOwnerEmail));

        outboxEventRepository.save(OutboxEvent.of(COMMAND_TOPIC, transaction.getId().toString(),
                TransactionCommand.of(transaction.getId(), accountId, amount, txType), objectMapper));

        log.info("Transaction initiated id={} accountId={} type={} amount={}",
                transaction.getId(), accountId, txType, amount);
        return transaction;
    }

    @KafkaListener(topics = "transaction.results", groupId = "transaction-service-results")
    @Transactional
    public void onTransactionResult(TransactionResult result) {
        Transaction transaction = transactionRepository.findById(result.transactionId())
                .orElseThrow(() -> new IllegalStateException("Transaction not found: " + result.transactionId()));

        if (transaction.getStatus() != Transaction.Status.PENDING) {
            log.info("Ignoring duplicate or stale result commandId={} for transactionId={} in state={}",
                    result.commandId(), transaction.getId(), transaction.getStatus());
            return;
        }

        if (result.status() == TransactionStatus.FAILED) {
            transaction.setStatus(Transaction.Status.FAILED);
            transaction.setFailureReason(result.reason());
            transaction.setUpdatedAt(Instant.now());
            transactionRepository.save(transaction);
            log.warn("Transaction id={} failed: {}", transaction.getId(), result.reason());
            return;
        }

        transaction.setStatus(Transaction.Status.COMPLETED);
        transaction.setUpdatedAt(Instant.now());
        transactionRepository.save(transaction);

        TransactionCompletedEvent txEvent = TransactionCompletedEvent.of(
                transaction.getId(), transaction.getAccountId(), transaction.getType(),
                transaction.getAmount(), transaction.getAccountOwnerEmail());
        outboxEventRepository.save(OutboxEvent.of(TX_COMPLETED_TOPIC, transaction.getId().toString(), txEvent, objectMapper));

        log.info("Transaction id={} completed newBalance={}", transaction.getId(), result.newBalance());
    }

    public Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
    }

    private TransactionType parseType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("Unknown transaction type: " + type);
        }
    }
}

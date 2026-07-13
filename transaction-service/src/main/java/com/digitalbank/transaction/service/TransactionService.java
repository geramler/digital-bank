package com.digitalbank.transaction.service;

import com.digitalbank.events.TransactionCommand;
import com.digitalbank.events.TransactionCompletedEvent;
import com.digitalbank.events.TransactionResult;
import com.digitalbank.events.TransactionStatus;
import com.digitalbank.events.TransactionType;
import com.digitalbank.transaction.model.Transaction;
import com.digitalbank.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final String COMMAND_TOPIC = "transaction.commands";
    private static final String TX_COMPLETED_TOPIC = "transaction.completed";

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionService(TransactionRepository transactionRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Transaction initiateTransaction(Long accountId, String type, BigDecimal amount) {
        TransactionType txType = parseType(type);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("accountId is required");
        }

        Transaction transaction = transactionRepository.save(new Transaction(accountId, txType.name(), amount));

        publish(COMMAND_TOPIC, transaction.getId(), TransactionCommand.of(
                transaction.getId(), accountId, amount, txType));

        log.info("Transaction initiated id={} accountId={} type={} amount={}",
                transaction.getId(), accountId, txType, amount);
        return transaction;
    }

    @org.springframework.kafka.annotation.KafkaListener(topics = "transaction.results", groupId = "transaction-service-results")
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
                transaction.getId(), transaction.getAccountId(), transaction.getType(), transaction.getAmount());
        publish(TX_COMPLETED_TOPIC, transaction.getId(), txEvent);

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

    private void publish(String topic, Long key, Object payload) {
        try {
            kafkaTemplate.send(topic, key.toString(), payload).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing to " + topic, ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not publish message to " + topic, ex);
        }
    }
}

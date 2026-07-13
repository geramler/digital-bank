package com.digitalbank.account.service;

import com.digitalbank.account.model.Account;
import com.digitalbank.account.model.ProcessedTransactionCommand;
import com.digitalbank.account.model.ProcessedTransferCommand;
import com.digitalbank.account.repository.AccountRepository;
import com.digitalbank.account.repository.ProcessedTransactionCommandRepository;
import com.digitalbank.account.repository.ProcessedTransferCommandRepository;
import com.digitalbank.events.AccountCreatedEvent;
import com.digitalbank.events.TransactionCommand;
import com.digitalbank.events.TransactionType;
import com.digitalbank.events.TransferCommand;
import com.digitalbank.events.TransferCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private static final String TOPIC = "account.created";
    private static final String BALANCE_CACHE_PREFIX = "account:balance:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final AccountRepository accountRepository;
    private final ProcessedTransferCommandRepository processedCommandRepository;
    private final ProcessedTransactionCommandRepository processedTransactionCommandRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public AccountService(
            AccountRepository accountRepository,
            ProcessedTransferCommandRepository processedCommandRepository,
            ProcessedTransactionCommandRepository processedTransactionCommandRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            RedisTemplate<String, String> redisTemplate) {
        this.accountRepository = accountRepository;
        this.processedCommandRepository = processedCommandRepository;
        this.processedTransactionCommandRepository = processedTransactionCommandRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Account createAccount(Long customerId, String accountType, BigDecimal initialBalance) {
        Account account = accountRepository.save(new Account(customerId, accountType, initialBalance));

        AccountCreatedEvent event = AccountCreatedEvent.of(account.getId(), account.getCustomerId(),
                account.getAccountType(), account.getBalance());
        kafkaTemplate.send(TOPIC, account.getId().toString(), event)
                .thenAccept(result -> log.info("Published {} to topic {} offset {}", event, TOPIC,
                        result.getRecordMetadata().offset()))
                .exceptionally(ex -> {
                    log.error("Failed to publish account.created event for accountId={}", account.getId(), ex);
                    return null;
                });

        cacheBalance(account.getId(), account.getBalance());
        log.info("Created account id={} customerId={} type={} balance={}",
                account.getId(), account.getCustomerId(), account.getAccountType(), account.getBalance());
        return account;
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long accountId) {
        String cacheKey = BALANCE_CACHE_PREFIX + accountId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for accountId={}", accountId);
            return new BigDecimal(cached);
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        cacheBalance(account.getId(), account.getBalance());
        return account.getBalance();
    }

    @Transactional
    public BigDecimal applyTransactionCommand(TransactionCommand command) {
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        var existing = processedTransactionCommandRepository.findByCommandId(command.commandId());
        if (existing.isPresent()) {
            log.info("Transaction command {} was already applied; returning recorded balance", command.commandId());
            return existing.get().getResultingBalance();
        }

        Account account = accountRepository.findByIdForUpdate(command.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.accountId()));

        BigDecimal newBalance;
        if (command.type() == TransactionType.WITHDRAWAL) {
            if (account.getBalance().compareTo(command.amount()) < 0) {
                throw new IllegalStateException("Insufficient funds for account " + command.accountId());
            }
            newBalance = account.getBalance().subtract(command.amount());
        } else {
            newBalance = account.getBalance().add(command.amount());
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
        processedTransactionCommandRepository.save(new ProcessedTransactionCommand(
                command.commandId(),
                command.transactionId(),
                command.type().name(),
                newBalance
        ));
        cacheBalance(account.getId(), newBalance);
        log.info(
                "Applied transaction command id={} type={} transactionId={} accountId={} newBalance={}",
                command.commandId(),
                command.type(),
                command.transactionId(),
                command.accountId(),
                newBalance
        );
        return newBalance;
    }

    @Transactional
    public void applyTransferCommand(TransferCommand command) {
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (processedCommandRepository.existsById(command.commandId())) {
            log.info("Transfer command {} was already applied; treating delivery as idempotent", command.commandId());
            return;
        }

        Account account = accountRepository.findByIdForUpdate(command.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.accountId()));

        BigDecimal newBalance;
        if (command.type() == TransferCommandType.DEBIT) {
            if (account.getBalance().compareTo(command.amount()) < 0) {
                throw new IllegalStateException("Insufficient funds for account " + command.accountId());
            }
            newBalance = account.getBalance().subtract(command.amount());
        } else {
            newBalance = account.getBalance().add(command.amount());
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
        processedCommandRepository.save(new ProcessedTransferCommand(
                command.commandId(),
                command.transferId(),
                command.type().name()
        ));
        cacheBalance(account.getId(), newBalance);
        log.info(
                "Applied transfer command id={} type={} transferId={} accountId={} newBalance={}",
                command.commandId(),
                command.type(),
                command.transferId(),
                command.accountId(),
                newBalance
        );
    }

    private void cacheBalance(Long accountId, BigDecimal balance) {
        redisTemplate.opsForValue().set(BALANCE_CACHE_PREFIX + accountId, balance.toPlainString(), CACHE_TTL);
    }
}
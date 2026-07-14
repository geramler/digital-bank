package com.digitalbank.account.service;

import com.digitalbank.account.model.Account;
import com.digitalbank.account.model.ProcessedTransactionCommand;
import com.digitalbank.account.model.ProcessedTransferCommand;
import com.digitalbank.account.outbox.OutboxEvent;
import com.digitalbank.account.outbox.OutboxEventRepository;
import com.digitalbank.account.repository.AccountRepository;
import com.digitalbank.account.repository.ProcessedTransactionCommandRepository;
import com.digitalbank.account.repository.ProcessedTransferCommandRepository;
import com.digitalbank.events.AccountCreatedEvent;
import com.digitalbank.events.TransactionCommand;
import com.digitalbank.events.TransactionResult;
import com.digitalbank.events.TransactionType;
import com.digitalbank.events.TransferCommand;
import com.digitalbank.events.TransferCommandType;
import com.digitalbank.events.TransferStepResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private static final String ACCOUNT_CREATED_TOPIC = "account.created";
    private static final String TX_RESULT_TOPIC = "transaction.results";
    private static final String TRANSFER_RESULT_TOPIC = "transfer.step-results";
    private static final String BALANCE_CACHE_PREFIX = "account:balance:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final AccountRepository accountRepository;
    private final ProcessedTransferCommandRepository processedTransferCommandRepository;
    private final ProcessedTransactionCommandRepository processedTransactionCommandRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public AccountService(
            AccountRepository accountRepository,
            ProcessedTransferCommandRepository processedTransferCommandRepository,
            ProcessedTransactionCommandRepository processedTransactionCommandRepository,
            OutboxEventRepository outboxEventRepository,
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.processedTransferCommandRepository = processedTransferCommandRepository;
        this.processedTransactionCommandRepository = processedTransactionCommandRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Account createAccount(Long customerId, String accountType, BigDecimal initialBalance, String customerEmail) {
        Account account = accountRepository.save(new Account(customerId, accountType, initialBalance, customerEmail));

        AccountCreatedEvent event = AccountCreatedEvent.of(
                account.getId(), account.getCustomerId(), account.getAccountType(), account.getBalance(), account.getCustomerEmail());
        outboxEventRepository.save(OutboxEvent.of(ACCOUNT_CREATED_TOPIC, account.getId().toString(), event, objectMapper));

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
    public void applyTransactionCommand(TransactionCommand command) {
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            outboxEventRepository.save(OutboxEvent.of(TX_RESULT_TOPIC, command.transactionId().toString(),
                    TransactionResult.failed(command, "Transaction amount must be positive"), objectMapper));
            return;
        }

        var existing = processedTransactionCommandRepository.findByCommandId(command.commandId());
        if (existing.isPresent()) {
            log.info("Transaction command {} was already applied; skipping duplicate", command.commandId());
            return;
        }

        Account account = accountRepository.findByIdForUpdate(command.accountId()).orElse(null);
        if (account == null) {
            outboxEventRepository.save(OutboxEvent.of(TX_RESULT_TOPIC, command.transactionId().toString(),
                    TransactionResult.failed(command, "Account not found: " + command.accountId()), objectMapper));
            return;
        }

        BigDecimal newBalance;
        if (command.type() == TransactionType.WITHDRAWAL) {
            if (account.getBalance().compareTo(command.amount()) < 0) {
                outboxEventRepository.save(OutboxEvent.of(TX_RESULT_TOPIC, command.transactionId().toString(),
                        TransactionResult.failed(command, "Insufficient funds for account " + command.accountId()), objectMapper));
                return;
            }
            newBalance = account.getBalance().subtract(command.amount());
        } else {
            newBalance = account.getBalance().add(command.amount());
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
        processedTransactionCommandRepository.save(new ProcessedTransactionCommand(
                command.commandId(), command.transactionId(), command.type().name(), newBalance));
        cacheBalance(account.getId(), newBalance);

        outboxEventRepository.save(OutboxEvent.of(TX_RESULT_TOPIC, command.transactionId().toString(),
                TransactionResult.succeeded(command, newBalance), objectMapper));

        log.info("Applied transaction command id={} type={} transactionId={} accountId={} newBalance={}",
                command.commandId(), command.type(), command.transactionId(), command.accountId(), newBalance);
    }

    @Transactional
    public void applyTransferCommand(TransferCommand command) {
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            outboxEventRepository.save(OutboxEvent.of(TRANSFER_RESULT_TOPIC, command.transferId().toString(),
                    TransferStepResult.failed(command, "Transfer amount must be positive"), objectMapper));
            return;
        }

        if (processedTransferCommandRepository.existsById(command.commandId())) {
            log.info("Transfer command {} was already applied; skipping duplicate", command.commandId());
            return;
        }

        Account account = accountRepository.findByIdForUpdate(command.accountId()).orElse(null);
        if (account == null) {
            outboxEventRepository.save(OutboxEvent.of(TRANSFER_RESULT_TOPIC, command.transferId().toString(),
                    TransferStepResult.failed(command, "Account not found: " + command.accountId()), objectMapper));
            return;
        }

        BigDecimal newBalance;
        if (command.type() == TransferCommandType.DEBIT) {
            if (account.getBalance().compareTo(command.amount()) < 0) {
                outboxEventRepository.save(OutboxEvent.of(TRANSFER_RESULT_TOPIC, command.transferId().toString(),
                        TransferStepResult.failed(command, "Insufficient funds for account " + command.accountId()), objectMapper));
                return;
            }
            newBalance = account.getBalance().subtract(command.amount());
        } else {
            newBalance = account.getBalance().add(command.amount());
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
        processedTransferCommandRepository.save(new ProcessedTransferCommand(
                command.commandId(), command.transferId(), command.type().name()));
        cacheBalance(account.getId(), newBalance);

        outboxEventRepository.save(OutboxEvent.of(TRANSFER_RESULT_TOPIC, command.transferId().toString(),
                TransferStepResult.succeeded(command, null), objectMapper));

        log.info("Applied transfer command id={} type={} transferId={} accountId={} newBalance={}",
                command.commandId(), command.type(), command.transferId(), command.accountId(), newBalance);
    }

    private void cacheBalance(Long accountId, BigDecimal balance) {
        redisTemplate.opsForValue().set(BALANCE_CACHE_PREFIX + accountId, balance.toPlainString(), CACHE_TTL);
    }
}

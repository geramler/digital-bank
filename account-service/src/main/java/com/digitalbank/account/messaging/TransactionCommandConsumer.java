package com.digitalbank.account.messaging;

import com.digitalbank.account.service.AccountService;
import com.digitalbank.events.TransactionCommand;
import com.digitalbank.events.TransactionResult;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionCommandConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionCommandConsumer.class);
    private static final String RESULT_TOPIC = "transaction.results";

    private final AccountService accountService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionCommandConsumer(
            AccountService accountService,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "transaction.commands", groupId = "account-service-transaction-commands")
    public void onTransactionCommand(TransactionCommand command) {
        TransactionResult result;
        try {
            BigDecimal newBalance = accountService.applyTransactionCommand(command);
            result = TransactionResult.succeeded(command, newBalance);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn(
                    "Transaction command id={} type={} transactionId={} failed: {}",
                    command.commandId(),
                    command.type(),
                    command.transactionId(),
                    ex.getMessage()
            );
            result = TransactionResult.failed(command, ex.getMessage());
        }

        publishResult(result);
    }

    private void publishResult(TransactionResult result) {
        try {
            kafkaTemplate.send(RESULT_TOPIC, result.transactionId().toString(), result)
                    .get(10, TimeUnit.SECONDS);
            log.info(
                    "Published transaction result commandId={} transactionId={} status={}",
                    result.commandId(),
                    result.transactionId(),
                    result.status()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while publishing transaction result for command " + result.commandId(),
                    ex
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not publish transaction result for command " + result.commandId(),
                    ex
            );
        }
    }
}

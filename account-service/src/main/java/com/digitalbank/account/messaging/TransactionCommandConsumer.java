package com.digitalbank.account.messaging;

import com.digitalbank.account.service.AccountService;
import com.digitalbank.events.TransactionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionCommandConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionCommandConsumer.class);

    private final AccountService accountService;

    public TransactionCommandConsumer(AccountService accountService) {
        this.accountService = accountService;
    }

    @KafkaListener(topics = "transaction.commands", groupId = "account-service-transaction-commands")
    public void onTransactionCommand(TransactionCommand command) {
        log.debug("Received transaction command id={} type={} transactionId={}",
                command.commandId(), command.type(), command.transactionId());
        accountService.applyTransactionCommand(command);
    }
}

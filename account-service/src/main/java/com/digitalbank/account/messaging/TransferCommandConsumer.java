package com.digitalbank.account.messaging;

import com.digitalbank.account.service.AccountService;
import com.digitalbank.events.TransferCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferCommandConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferCommandConsumer.class);

    private final AccountService accountService;

    public TransferCommandConsumer(AccountService accountService) {
        this.accountService = accountService;
    }

    @KafkaListener(topics = "transfer.commands", groupId = "account-service-transfer-commands")
    public void onTransferCommand(TransferCommand command) {
        log.debug("Received transfer command id={} type={} transferId={}",
                command.commandId(), command.type(), command.transferId());
        accountService.applyTransferCommand(command);
    }
}

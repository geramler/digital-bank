package com.digitalbank.account.messaging;

import com.digitalbank.account.service.AccountService;
import com.digitalbank.events.TransferCommand;
import com.digitalbank.events.TransferStepResult;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransferCommandConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferCommandConsumer.class);
    private static final String RESULT_TOPIC = "transfer.step-results";

    private final AccountService accountService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransferCommandConsumer(
            AccountService accountService,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "transfer.commands", groupId = "account-service-transfer-commands")
    public void onTransferCommand(TransferCommand command) {
        TransferStepResult result;
        try {
            accountService.applyTransferCommand(command);
            result = TransferStepResult.succeeded(command, null);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn(
                    "Transfer command id={} type={} transferId={} failed: {}",
                    command.commandId(),
                    command.type(),
                    command.transferId(),
                    ex.getMessage()
            );
            result = TransferStepResult.failed(command, ex.getMessage());
        }

        publishResult(result);
    }

    private void publishResult(TransferStepResult result) {
        try {
            kafkaTemplate.send(RESULT_TOPIC, result.transferId().toString(), result)
                    .get(10, TimeUnit.SECONDS);
            log.info(
                    "Published transfer step result commandId={} transferId={} type={} status={}",
                    result.commandId(),
                    result.transferId(),
                    result.type(),
                    result.status()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while publishing transfer step result for command " + result.commandId(),
                    ex
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not publish transfer step result for command " + result.commandId(),
                    ex
            );
        }
    }
}
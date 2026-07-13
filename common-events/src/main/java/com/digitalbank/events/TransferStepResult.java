package com.digitalbank.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record TransferStepResult(
    @JsonProperty("commandId") UUID commandId,
    @JsonProperty("transferId") Long transferId,
    @JsonProperty("type") TransferCommandType type,
    @JsonProperty("status") TransferStepStatus status,
    @JsonProperty("transactionId") Long transactionId,
    @JsonProperty("reason") String reason,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static TransferStepResult succeeded(TransferCommand command, Long transactionId) {
        return new TransferStepResult(
            command.commandId(),
            command.transferId(),
            command.type(),
            TransferStepStatus.SUCCEEDED,
            transactionId,
            null,
            Instant.now()
        );
    }

    public static TransferStepResult failed(TransferCommand command, String reason) {
        return new TransferStepResult(
            command.commandId(),
            command.transferId(),
            command.type(),
            TransferStepStatus.FAILED,
            null,
            reason,
            Instant.now()
        );
    }
}
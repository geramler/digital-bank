package com.digitalbank.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResult(
    @JsonProperty("commandId") UUID commandId,
    @JsonProperty("transactionId") Long transactionId,
    @JsonProperty("accountId") Long accountId,
    @JsonProperty("type") TransactionType type,
    @JsonProperty("status") TransactionStatus status,
    @JsonProperty("newBalance") BigDecimal newBalance,
    @JsonProperty("reason") String reason,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static TransactionResult succeeded(TransactionCommand command, BigDecimal newBalance) {
        return new TransactionResult(
            command.commandId(),
            command.transactionId(),
            command.accountId(),
            command.type(),
            TransactionStatus.COMPLETED,
            newBalance,
            null,
            Instant.now()
        );
    }

    public static TransactionResult failed(TransactionCommand command, String reason) {
        return new TransactionResult(
            command.commandId(),
            command.transactionId(),
            command.accountId(),
            command.type(),
            TransactionStatus.FAILED,
            null,
            reason,
            Instant.now()
        );
    }
}

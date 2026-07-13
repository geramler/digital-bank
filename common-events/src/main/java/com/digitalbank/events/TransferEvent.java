package com.digitalbank.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferEvent(
    @JsonProperty("eventId") UUID eventId,
    @JsonProperty("transferId") Long transferId,
    @JsonProperty("fromAccountId") Long fromAccountId,
    @JsonProperty("toAccountId") Long toAccountId,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("type") TransferEventType type,
    @JsonProperty("reason") String reason,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static TransferEvent initiated(
            Long transferId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount) {
        return create(transferId, fromAccountId, toAccountId, amount, TransferEventType.INITIATED, null);
    }

    public static TransferEvent completed(
            Long transferId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount) {
        return create(transferId, fromAccountId, toAccountId, amount, TransferEventType.COMPLETED, null);
    }

    public static TransferEvent failed(
            Long transferId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            String reason) {
        return create(transferId, fromAccountId, toAccountId, amount, TransferEventType.FAILED, reason);
    }

    private static TransferEvent create(
            Long transferId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            TransferEventType type,
            String reason) {
        return new TransferEvent(
            UUID.randomUUID(),
            transferId,
            fromAccountId,
            toAccountId,
            amount,
            type,
            reason,
            Instant.now()
        );
    }
}
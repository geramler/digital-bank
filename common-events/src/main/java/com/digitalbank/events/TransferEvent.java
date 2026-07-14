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
    @JsonProperty("fromAccountEmail") String fromAccountEmail,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static TransferEvent initiated(
            Long transferId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            String fromAccountEmail) {
        return create(transferId, fromAccountId, toAccountId, amount, TransferEventType.INITIATED, null, fromAccountEmail);
    }

    public static TransferEvent completed(
            Long transferId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            String fromAccountEmail) {
        return create(transferId, fromAccountId, toAccountId, amount, TransferEventType.COMPLETED, null, fromAccountEmail);
    }

    public static TransferEvent failed(
            Long transferId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            String reason,
            String fromAccountEmail) {
        return create(transferId, fromAccountId, toAccountId, amount, TransferEventType.FAILED, reason, fromAccountEmail);
    }

    private static TransferEvent create(
            Long transferId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            TransferEventType type,
            String reason,
            String fromAccountEmail) {
        return new TransferEvent(
            UUID.randomUUID(),
            transferId,
            fromAccountId,
            toAccountId,
            amount,
            type,
            reason,
            fromAccountEmail,
            Instant.now()
        );
    }
}
package com.digitalbank.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCommand(
    @JsonProperty("commandId") UUID commandId,
    @JsonProperty("transactionId") Long transactionId,
    @JsonProperty("accountId") Long accountId,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("type") TransactionType type,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static TransactionCommand of(Long transactionId, Long accountId, BigDecimal amount, TransactionType type) {
        return new TransactionCommand(
            UUID.nameUUIDFromBytes(("transaction:" + transactionId).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            transactionId,
            accountId,
            amount,
            type,
            Instant.now()
        );
    }
}

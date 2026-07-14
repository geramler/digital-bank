package com.digitalbank.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCompletedEvent(
    @JsonProperty("eventId") UUID eventId,
    @JsonProperty("transactionId") Long transactionId,
    @JsonProperty("accountId") Long accountId,
    @JsonProperty("type") String type,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("accountOwnerEmail") String accountOwnerEmail,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static TransactionCompletedEvent of(Long transactionId, Long accountId, String type, BigDecimal amount, String accountOwnerEmail) {
        return new TransactionCompletedEvent(UUID.randomUUID(), transactionId, accountId, type, amount, accountOwnerEmail, Instant.now());
    }
}
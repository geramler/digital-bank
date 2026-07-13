package com.digitalbank.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountCreatedEvent(
    @JsonProperty("eventId") UUID eventId,
    @JsonProperty("accountId") Long accountId,
    @JsonProperty("customerId") Long customerId,
    @JsonProperty("accountType") String accountType,
    @JsonProperty("initialBalance") BigDecimal initialBalance,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static AccountCreatedEvent of(Long accountId, Long customerId, String accountType, BigDecimal initialBalance) {
        return new AccountCreatedEvent(UUID.randomUUID(), accountId, customerId, accountType, initialBalance, Instant.now());
    }
}
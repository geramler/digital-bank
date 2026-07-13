package com.digitalbank.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record CustomerCreatedEvent(
    @JsonProperty("eventId") UUID eventId,
    @JsonProperty("customerId") Long customerId,
    @JsonProperty("email") String email,
    @JsonProperty("name") String name,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static CustomerCreatedEvent of(Long customerId, String email, String name) {
        return new CustomerCreatedEvent(UUID.randomUUID(), customerId, email, name, Instant.now());
    }
}
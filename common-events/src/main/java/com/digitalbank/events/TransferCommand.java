package com.digitalbank.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCommand(
    @JsonProperty("commandId") UUID commandId,
    @JsonProperty("transferId") Long transferId,
    @JsonProperty("accountId") Long accountId,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("type") TransferCommandType type,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static TransferCommand debit(Long transferId, Long accountId, BigDecimal amount) {
        return new TransferCommand(
            deterministicId(transferId, TransferCommandType.DEBIT),
            transferId,
            accountId,
            amount,
            TransferCommandType.DEBIT,
            Instant.now()
        );
    }

    public static TransferCommand credit(Long transferId, Long accountId, BigDecimal amount) {
        return new TransferCommand(
            deterministicId(transferId, TransferCommandType.CREDIT),
            transferId,
            accountId,
            amount,
            TransferCommandType.CREDIT,
            Instant.now()
        );
    }

    public static TransferCommand refund(Long transferId, Long accountId, BigDecimal amount) {
        return new TransferCommand(
            deterministicId(transferId, TransferCommandType.REFUND),
            transferId,
            accountId,
            amount,
            TransferCommandType.REFUND,
            Instant.now()
        );
    }

    private static UUID deterministicId(Long transferId, TransferCommandType type) {
        return UUID.nameUUIDFromBytes(("transfer:" + transferId + ":" + type).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
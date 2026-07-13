package com.digitalbank.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_transaction_commands")
public class ProcessedTransactionCommand {

    @Id
    private UUID commandId;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private String commandType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal resultingBalance;

    @Column(nullable = false, updatable = false)
    private Instant processedAt;

    protected ProcessedTransactionCommand() {
    }

    public ProcessedTransactionCommand(UUID commandId, Long transactionId, String commandType, BigDecimal resultingBalance) {
        this.commandId = commandId;
        this.transactionId = transactionId;
        this.commandType = commandType;
        this.resultingBalance = resultingBalance;
        this.processedAt = Instant.now();
    }

    public UUID getCommandId() {
        return commandId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getCommandType() {
        return commandType;
    }

    public BigDecimal getResultingBalance() {
        return resultingBalance;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}

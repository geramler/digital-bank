package com.digitalbank.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_transfer_commands")
public class ProcessedTransferCommand {

    @Id
    private UUID commandId;

    @Column(nullable = false)
    private Long transferId;

    @Column(nullable = false)
    private String commandType;

    @Column(nullable = false, updatable = false)
    private Instant processedAt;

    protected ProcessedTransferCommand() {
    }

    public ProcessedTransferCommand(UUID commandId, Long transferId, String commandType) {
        this.commandId = commandId;
        this.transferId = transferId;
        this.commandType = commandType;
        this.processedAt = Instant.now();
    }

    public UUID getCommandId() {
        return commandId;
    }

    public Long getTransferId() {
        return transferId;
    }

    public String getCommandType() {
        return commandType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
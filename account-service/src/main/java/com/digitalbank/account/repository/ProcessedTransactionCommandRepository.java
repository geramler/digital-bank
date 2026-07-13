package com.digitalbank.account.repository;

import com.digitalbank.account.model.ProcessedTransactionCommand;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedTransactionCommandRepository extends JpaRepository<ProcessedTransactionCommand, UUID> {
    Optional<ProcessedTransactionCommand> findByCommandId(UUID commandId);
}

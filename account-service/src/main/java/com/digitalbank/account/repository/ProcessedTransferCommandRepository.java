package com.digitalbank.account.repository;

import com.digitalbank.account.model.ProcessedTransferCommand;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedTransferCommandRepository extends JpaRepository<ProcessedTransferCommand, UUID> {
}
package com.digitalbank.transfer.repository;

import com.digitalbank.transfer.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}
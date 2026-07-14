package com.digitalbank.transfer.controller;

import com.digitalbank.transfer.dto.CreateTransferRequest;
import com.digitalbank.transfer.model.Transfer;
import com.digitalbank.transfer.saga.TransferSagaOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferSagaOrchestrator sagaOrchestrator;

    public TransferController(TransferSagaOrchestrator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @GetMapping("/health")
    public String health() {
        return "transfer-service is up";
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("service", "transfer-service", "status", "UP");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> createTransfer(@Valid @RequestBody CreateTransferRequest request) {
        Transfer transfer = sagaOrchestrator.initiateTransfer(
                request.fromAccountId(), request.toAccountId(), request.amount(), request.fromAccountEmail());
        return Map.of(
            "id", transfer.getId(),
            "fromAccountId", transfer.getFromAccountId(),
            "toAccountId", transfer.getToAccountId(),
            "amount", transfer.getAmount().toPlainString(),
            "status", transfer.getStatus().name(),
            "createdAt", transfer.getCreatedAt().toString()
        );
    }
}

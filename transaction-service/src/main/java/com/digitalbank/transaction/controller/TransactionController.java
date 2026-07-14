package com.digitalbank.transaction.controller;

import com.digitalbank.transaction.dto.CreateTransactionRequest;
import com.digitalbank.transaction.model.Transaction;
import com.digitalbank.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/health")
    public String health() {
        return "transaction-service is up";
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("service", "transaction-service", "status", "UP");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction tx = transactionService.initiateTransaction(
                request.accountId(), request.type(), request.amount(), request.accountOwnerEmail());
        return toResponse(tx);
    }

    @GetMapping("/{transactionId}")
    public Map<String, Object> getTransaction(@PathVariable Long transactionId) {
        return toResponse(transactionService.getTransaction(transactionId));
    }

    private Map<String, Object> toResponse(Transaction tx) {
        return Map.of(
            "id", tx.getId(),
            "accountId", tx.getAccountId(),
            "type", tx.getType(),
            "amount", tx.getAmount().toPlainString(),
            "status", tx.getStatus().name(),
            "createdAt", tx.getCreatedAt().toString()
        );
    }
}

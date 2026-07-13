package com.digitalbank.transfer.controller;

import com.digitalbank.transfer.model.Transfer;
import com.digitalbank.transfer.saga.TransferSagaOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
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
    public Map<String, Object> createTransfer(@RequestBody Map<String, Object> request) {
        Long fromAccountId = toLong(request.get("fromAccountId"));
        Long toAccountId = toLong(request.get("toAccountId"));
        BigDecimal amount = toBigDecimal(request.getOrDefault("amount", 0));

        Transfer transfer = sagaOrchestrator.initiateTransfer(fromAccountId, toAccountId, amount);

        return Map.of(
            "id", transfer.getId(),
            "fromAccountId", transfer.getFromAccountId(),
            "toAccountId", transfer.getToAccountId(),
            "amount", transfer.getAmount().toPlainString(),
            "status", transfer.getStatus().name(),
            "createdAt", transfer.getCreatedAt().toString()
        );
    }

    private Long toLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        return 1L;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (value instanceof String s) return new BigDecimal(s);
        return BigDecimal.ZERO;
    }
}
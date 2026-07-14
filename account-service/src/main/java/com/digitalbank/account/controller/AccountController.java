package com.digitalbank.account.controller;

import com.digitalbank.account.dto.CreateAccountRequest;
import com.digitalbank.account.model.Account;
import com.digitalbank.account.service.AccountService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/health")
    public String health() {
        return "account-service is up";
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("service", "account-service", "status", "UP");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        String accountType = request.accountType() != null ? request.accountType() : "SAVINGS";
        BigDecimal initialBalance = request.initialBalance() != null ? request.initialBalance() : BigDecimal.ZERO;

        Account account = accountService.createAccount(
                request.customerId(), accountType, initialBalance, request.customerEmail());

        return Map.of(
            "id", account.getId(),
            "customerId", account.getCustomerId(),
            "accountType", account.getAccountType(),
            "balance", account.getBalance().toPlainString(),
            "createdAt", account.getCreatedAt().toString()
        );
    }

    @GetMapping("/{accountId}/balance")
    public Map<String, Object> getBalance(@PathVariable Long accountId) {
        BigDecimal balance = accountService.getBalance(accountId);
        return Map.of("accountId", accountId, "balance", balance.toPlainString());
    }
}

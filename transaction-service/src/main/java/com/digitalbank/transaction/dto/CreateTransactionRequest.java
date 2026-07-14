package com.digitalbank.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull Long accountId,
        @NotBlank String type,
        @NotNull @Positive BigDecimal amount,
        String accountOwnerEmail
) {}

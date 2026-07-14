package com.digitalbank.account.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull Long customerId,
        String accountType,
        @PositiveOrZero BigDecimal initialBalance,
        String customerEmail
) {}

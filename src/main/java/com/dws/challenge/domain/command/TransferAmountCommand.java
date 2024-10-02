package com.dws.challenge.domain.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Transfer amount command which holds the accountId of payer and receiver bank accounts.
 */
public record TransferAmountCommand(
    @NotNull(message = "AccountFromId must not be null")
    String accountFromId,
    @NotNull(message = "AccountFromId must not be null")
    String accountToId,
    @NotNull(message = "Transfer amount must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Transfer amount must be greater than zero")
    BigDecimal amount) {
}
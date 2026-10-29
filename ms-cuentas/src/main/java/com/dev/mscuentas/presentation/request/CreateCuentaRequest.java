package com.dev.mscuentas.presentation.request;

import com.dev.mscuentas.application.dto.CreateCuentaCommand;
import com.dev.mscuentas.domain.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateCuentaRequest(
        @NotBlank(message = "es obligatorio") String numeroCuenta,
        @NotNull(message = "es obligatorio (AHORROS o CORRIENTE)") AccountType tipoCuenta,
        @NotNull(message = "es obligatorio") @DecimalMin(value = "0.00", message = "no puede ser negativo") BigDecimal saldoInicial,
        @NotBlank(message = "es obligatorio") String clienteId) {

    public CreateCuentaCommand toCommand() {
        return new CreateCuentaCommand(numeroCuenta, tipoCuenta, saldoInicial, clienteId);
    }
}

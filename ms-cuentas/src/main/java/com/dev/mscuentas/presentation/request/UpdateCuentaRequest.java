package com.dev.mscuentas.presentation.request;

import com.dev.mscuentas.application.dto.UpdateCuentaCommand;
import com.dev.mscuentas.domain.model.AccountType;
import jakarta.validation.constraints.NotNull;

public record UpdateCuentaRequest(
        @NotNull(message = "es obligatorio (AHORROS o CORRIENTE)") AccountType tipoCuenta,
        @NotNull(message = "es obligatorio") Boolean estado) {

    public UpdateCuentaCommand toCommand(String numeroCuenta) {
        return new UpdateCuentaCommand(numeroCuenta, tipoCuenta, estado);
    }
}

package com.dev.mscuentas.presentation.request;

import com.dev.mscuentas.application.dto.RegisterMovimientoCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** {@code valor} positive is a deposit and negative a withdrawal. {@code fecha} (ISO-8601) is optional. */
public record RegisterMovimientoRequest(
        @NotBlank(message = "es obligatorio") String numeroCuenta,
        @NotNull(message = "es obligatorio") BigDecimal valor,
        LocalDateTime fecha) {

    public RegisterMovimientoCommand toCommand() {
        return new RegisterMovimientoCommand(numeroCuenta, valor, fecha);
    }
}

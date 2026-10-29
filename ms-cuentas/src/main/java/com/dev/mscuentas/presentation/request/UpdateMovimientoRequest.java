package com.dev.mscuentas.presentation.request;

import com.dev.mscuentas.application.dto.UpdateMovimientoCommand;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateMovimientoRequest(@NotNull(message = "es obligatorio") LocalDateTime fecha) {

    public UpdateMovimientoCommand toCommand(Long id) {
        return new UpdateMovimientoCommand(id, fecha);
    }
}

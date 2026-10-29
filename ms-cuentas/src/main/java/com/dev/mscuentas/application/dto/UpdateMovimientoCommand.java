package com.dev.mscuentas.application.dto;

import java.time.LocalDateTime;

public record UpdateMovimientoCommand(Long id, LocalDateTime fecha) {
}

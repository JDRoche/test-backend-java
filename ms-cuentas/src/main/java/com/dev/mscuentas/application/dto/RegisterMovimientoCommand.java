package com.dev.mscuentas.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** {@code fecha} is optional: when omitted the movement is dated now. */
public record RegisterMovimientoCommand(String numeroCuenta, BigDecimal valor, LocalDateTime fecha) {
}

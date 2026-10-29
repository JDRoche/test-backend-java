package com.dev.mscuentas.presentation.response;

import com.dev.mscuentas.application.dto.MovimientoResult;
import com.dev.mscuentas.domain.model.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(Long id, String numeroCuenta, LocalDateTime fecha, MovementType tipoMovimiento,
                                 BigDecimal valor, BigDecimal saldo) {

    public static MovimientoResponse from(MovimientoResult result) {
        return new MovimientoResponse(result.id(), result.numeroCuenta(), result.fecha(),
                result.tipoMovimiento(), result.valor(), result.saldo());
    }
}

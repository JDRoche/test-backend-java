package com.dev.mscuentas.application.dto;

import com.dev.mscuentas.domain.model.MovementType;
import com.dev.mscuentas.domain.model.Movimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResult(Long id, String numeroCuenta, LocalDateTime fecha, MovementType tipoMovimiento,
                               BigDecimal valor, BigDecimal saldo) {

    public static MovimientoResult from(Movimiento movimiento) {
        return new MovimientoResult(movimiento.getId(), movimiento.getNumeroCuenta(), movimiento.getFecha(),
                movimiento.getTipoMovimiento(), movimiento.getValor(), movimiento.getSaldo());
    }
}

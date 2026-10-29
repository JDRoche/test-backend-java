package com.dev.mscuentas.domain.model;

import com.dev.mscuentas.domain.exception.DomainValidationException;

import java.math.BigDecimal;

/** The type is derived from the sign of the valor: positive is a deposit, negative a withdrawal. */
public enum MovementType {
    DEPOSITO,
    RETIRO;

    public static MovementType from(BigDecimal valor) {
        return switch (valor.signum()) {
            case 1 -> DEPOSITO;
            case -1 -> RETIRO;
            default -> throw new DomainValidationException("El valor del movimiento no puede ser cero");
        };
    }
}

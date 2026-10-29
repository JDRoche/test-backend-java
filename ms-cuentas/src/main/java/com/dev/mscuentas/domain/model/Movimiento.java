package com.dev.mscuentas.domain.model;

import com.dev.mscuentas.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record of a transaction. {@code saldo} is the account balance right after applying it, so valor and
 * saldo never change once registered; only the fecha can be corrected.
 */
public class Movimiento {

    private final Long id;
    private final Long cuentaId;
    private final String numeroCuenta;
    private LocalDateTime fecha;
    private final MovementType tipoMovimiento;
    private final BigDecimal valor;
    private final BigDecimal saldo;

    private Movimiento(Long id, Long cuentaId, String numeroCuenta, LocalDateTime fecha,
                       MovementType tipoMovimiento, BigDecimal valor, BigDecimal saldo) {
        this.id = id;
        this.cuentaId = cuentaId;
        this.numeroCuenta = numeroCuenta;
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.valor = valor;
        this.saldo = saldo;
    }

    static Movimiento create(Long cuentaId, String numeroCuenta, LocalDateTime fecha,
                             BigDecimal valor, BigDecimal saldo) {
        return new Movimiento(null, cuentaId, numeroCuenta, fecha, MovementType.from(valor), valor, saldo);
    }

    public static Movimiento rehydrate(Long id, Long cuentaId, String numeroCuenta, LocalDateTime fecha,
                                       MovementType tipoMovimiento, BigDecimal valor, BigDecimal saldo) {
        return new Movimiento(id, cuentaId, numeroCuenta, fecha, tipoMovimiento, valor, saldo);
    }

    public void reschedule(LocalDateTime newFecha) {
        if (newFecha == null) {
            throw new DomainValidationException("La fecha es obligatoria");
        }
        this.fecha = newFecha;
    }

    public Long getId() {
        return id;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public MovementType getTipoMovimiento() {
        return tipoMovimiento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}

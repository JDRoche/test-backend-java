package com.dev.mscuentas.domain.model;

import com.dev.mscuentas.domain.exception.DomainValidationException;
import com.dev.mscuentas.domain.exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Cuenta {

    private static final int MONEY_SCALE = 2;

    private final Long id;
    private final String numeroCuenta;
    private AccountType tipoCuenta;
    private final BigDecimal saldoInicial;
    private BigDecimal currentBalance;
    private boolean estado;
    private final String clienteId;

    private Cuenta(Long id, String numeroCuenta, AccountType tipoCuenta, BigDecimal saldoInicial,
                   BigDecimal currentBalance, boolean estado, String clienteId) {
        this.id = id;
        this.numeroCuenta = required(numeroCuenta, "El número de cuenta");
        this.tipoCuenta = requireNonNull(tipoCuenta, "El tipo de cuenta");
        this.saldoInicial = nonNegativeMoney(saldoInicial, "El saldo inicial");
        this.currentBalance = currentBalance;
        this.estado = estado;
        this.clienteId = required(clienteId, "El clienteId");
    }

    /** Opens an active account whose balance equals the saldo inicial. */
    public static Cuenta create(String numeroCuenta, AccountType tipoCuenta, BigDecimal saldoInicial, String clienteId) {
        BigDecimal initial = nonNegativeMoney(saldoInicial, "El saldo inicial");
        return new Cuenta(null, numeroCuenta, tipoCuenta, initial, initial, true, clienteId);
    }

    /** Rebuilds an already persisted cuenta (meant for outbound adapters only). */
    public static Cuenta rehydrate(Long id, String numeroCuenta, AccountType tipoCuenta, BigDecimal saldoInicial,
                                   BigDecimal currentBalance, boolean estado, String clienteId) {
        return new Cuenta(id, numeroCuenta, tipoCuenta, saldoInicial, currentBalance, estado, clienteId);
    }

    public void update(AccountType tipoCuenta, boolean estado) {
        this.tipoCuenta = requireNonNull(tipoCuenta, "El tipo de cuenta");
        this.estado = estado;
    }

    /**
     * Applies a movement: a positive valor is a deposit and a negative one a withdrawal.
     * The balance is only changed when the movement is valid, so a rejected one leaves no trace.
     */
    public Movimiento register(BigDecimal valor, LocalDateTime fecha) {
        if (!estado) {
            throw new DomainValidationException("La cuenta " + numeroCuenta + " no está activa");
        }
        BigDecimal amount = money(valor, "El valor del movimiento");
        MovementType.from(amount);

        BigDecimal newBalance = currentBalance.add(amount);
        if (newBalance.signum() < 0) {
            throw new InsufficientBalanceException();
        }

        this.currentBalance = newBalance;
        return Movimiento.create(id, numeroCuenta, fecha != null ? fecha : LocalDateTime.now(), amount, newBalance);
    }

    private static String required(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(fieldLabel + " es obligatorio");
        }
        return value.trim();
    }

    private static <T> T requireNonNull(T value, String fieldLabel) {
        if (value == null) {
            throw new DomainValidationException(fieldLabel + " es obligatorio");
        }
        return value;
    }

    private static BigDecimal nonNegativeMoney(BigDecimal value, String fieldLabel) {
        BigDecimal money = money(value, fieldLabel);
        if (money.signum() < 0) {
            throw new DomainValidationException(fieldLabel + " no puede ser negativo");
        }
        return money;
    }

    /** Normalizes to 2 decimals and rejects values that would lose precision. */
    private static BigDecimal money(BigDecimal value, String fieldLabel) {
        requireNonNull(value, fieldLabel);
        if (value.stripTrailingZeros().scale() > MONEY_SCALE) {
            throw new DomainValidationException(fieldLabel + " admite como máximo " + MONEY_SCALE + " decimales");
        }
        return value.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    public Long getId() {
        return id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public AccountType getTipoCuenta() {
        return tipoCuenta;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public boolean isEstado() {
        return estado;
    }

    public String getClienteId() {
        return clienteId;
    }
}

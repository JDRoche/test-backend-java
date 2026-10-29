package com.dev.mscuentas.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read model of the account statement: one movement together with the data of its cuenta.
 * {@code saldo} is the balance right after the movement ("Saldo Disponible" in the report).
 */
public record StatementLine(LocalDateTime fecha, String numeroCuenta, AccountType tipoCuenta,
                            BigDecimal saldoInicial, boolean estado, BigDecimal valor, BigDecimal saldo) {
}

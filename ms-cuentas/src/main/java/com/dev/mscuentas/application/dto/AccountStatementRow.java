package com.dev.mscuentas.application.dto;

import com.dev.mscuentas.domain.model.AccountType;
import com.dev.mscuentas.domain.model.StatementLine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountStatementRow(LocalDateTime fecha, String cliente, String numeroCuenta, AccountType tipoCuenta,
                                  BigDecimal saldoInicial, boolean estado, BigDecimal valor, BigDecimal saldo) {

    public static AccountStatementRow of(String clienteNombre, StatementLine line) {
        return new AccountStatementRow(line.fecha(), clienteNombre, line.numeroCuenta(), line.tipoCuenta(),
                line.saldoInicial(), line.estado(), line.valor(), line.saldo());
    }
}

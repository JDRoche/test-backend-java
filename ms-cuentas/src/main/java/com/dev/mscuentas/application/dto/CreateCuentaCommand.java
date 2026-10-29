package com.dev.mscuentas.application.dto;

import com.dev.mscuentas.domain.model.AccountType;

import java.math.BigDecimal;

public record CreateCuentaCommand(String numeroCuenta, AccountType tipoCuenta, BigDecimal saldoInicial,
                                  String clienteId) {
}

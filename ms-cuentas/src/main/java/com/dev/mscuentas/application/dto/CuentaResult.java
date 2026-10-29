package com.dev.mscuentas.application.dto;

import com.dev.mscuentas.domain.model.AccountType;
import com.dev.mscuentas.domain.model.Cuenta;

import java.math.BigDecimal;

public record CuentaResult(String numeroCuenta, AccountType tipoCuenta, BigDecimal saldoInicial,
                           BigDecimal currentBalance, boolean estado, String clienteId) {

    public static CuentaResult from(Cuenta cuenta) {
        return new CuentaResult(cuenta.getNumeroCuenta(), cuenta.getTipoCuenta(), cuenta.getSaldoInicial(),
                cuenta.getCurrentBalance(), cuenta.isEstado(), cuenta.getClienteId());
    }
}

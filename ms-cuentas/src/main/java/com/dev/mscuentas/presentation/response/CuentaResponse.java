package com.dev.mscuentas.presentation.response;

import com.dev.mscuentas.application.dto.CuentaResult;
import com.dev.mscuentas.domain.model.AccountType;

import java.math.BigDecimal;

public record CuentaResponse(String numeroCuenta, AccountType tipoCuenta, BigDecimal saldoInicial,
                             BigDecimal currentBalance, boolean estado, String clienteId) {

    public static CuentaResponse from(CuentaResult result) {
        return new CuentaResponse(result.numeroCuenta(), result.tipoCuenta(), result.saldoInicial(),
                result.currentBalance(), result.estado(), result.clienteId());
    }
}

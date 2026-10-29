package com.dev.mscuentas.application.dto;

import com.dev.mscuentas.domain.model.AccountType;

public record UpdateCuentaCommand(String numeroCuenta, AccountType tipoCuenta, boolean estado) {
}

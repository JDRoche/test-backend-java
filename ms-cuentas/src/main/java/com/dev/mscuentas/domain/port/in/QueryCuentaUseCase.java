package com.dev.mscuentas.domain.port.in;

import com.dev.mscuentas.application.dto.CuentaResult;

import java.util.List;

public interface QueryCuentaUseCase {

    CuentaResult getByNumeroCuenta(String numeroCuenta);

    /** @param clienteId optional filter; when null every cuenta is returned */
    List<CuentaResult> list(String clienteId);
}

package com.dev.mscuentas.domain.port.in;

import com.dev.mscuentas.application.dto.MovimientoResult;

import java.util.List;

public interface QueryMovimientoUseCase {

    MovimientoResult getById(Long id);

    /** @param numeroCuenta optional filter; when null every movimiento is returned */
    List<MovimientoResult> list(String numeroCuenta);
}

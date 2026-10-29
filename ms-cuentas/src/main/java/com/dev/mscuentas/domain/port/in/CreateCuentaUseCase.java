package com.dev.mscuentas.domain.port.in;

import com.dev.mscuentas.application.dto.CreateCuentaCommand;
import com.dev.mscuentas.application.dto.CuentaResult;

public interface CreateCuentaUseCase {

    CuentaResult create(CreateCuentaCommand command);
}

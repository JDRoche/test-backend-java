package com.dev.mscuentas.domain.port.in;

import com.dev.mscuentas.application.dto.CuentaResult;
import com.dev.mscuentas.application.dto.UpdateCuentaCommand;

public interface UpdateCuentaUseCase {

    CuentaResult update(UpdateCuentaCommand command);
}

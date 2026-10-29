package com.dev.mscuentas.domain.port.in;

import com.dev.mscuentas.application.dto.MovimientoResult;
import com.dev.mscuentas.application.dto.RegisterMovimientoCommand;

public interface RegisterMovimientoUseCase {

    MovimientoResult register(RegisterMovimientoCommand command);
}

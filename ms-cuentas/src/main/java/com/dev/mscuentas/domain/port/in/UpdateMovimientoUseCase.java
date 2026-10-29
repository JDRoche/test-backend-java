package com.dev.mscuentas.domain.port.in;

import com.dev.mscuentas.application.dto.MovimientoResult;
import com.dev.mscuentas.application.dto.UpdateMovimientoCommand;

public interface UpdateMovimientoUseCase {

    MovimientoResult update(UpdateMovimientoCommand command);
}

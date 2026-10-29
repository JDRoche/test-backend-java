package com.dev.mscuentas.domain.port.in;

import com.dev.mscuentas.application.dto.SyncClienteCommand;

public interface SyncClienteUseCase {

    void sync(SyncClienteCommand command);
}

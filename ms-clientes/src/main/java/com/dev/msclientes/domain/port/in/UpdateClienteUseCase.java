package com.dev.msclientes.domain.port.in;

import com.dev.msclientes.application.dto.ClienteResult;
import com.dev.msclientes.application.dto.UpdateClienteCommand;

public interface UpdateClienteUseCase {

    ClienteResult update(UpdateClienteCommand command);
}

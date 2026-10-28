package com.dev.msclientes.domain.port.in;

import com.dev.msclientes.application.dto.ClienteResult;
import com.dev.msclientes.application.dto.CreateClienteCommand;

public interface CreateClienteUseCase {

    ClienteResult create(CreateClienteCommand command);
}

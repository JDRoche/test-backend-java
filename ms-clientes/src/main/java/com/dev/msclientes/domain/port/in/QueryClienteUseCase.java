package com.dev.msclientes.domain.port.in;

import com.dev.msclientes.application.dto.ClienteResult;

import java.util.List;

public interface QueryClienteUseCase {

    ClienteResult getByClienteId(String clienteId);

    List<ClienteResult> list();
}

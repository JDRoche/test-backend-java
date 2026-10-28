package com.dev.msclientes.domain.port.out;

import com.dev.msclientes.domain.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

    Cliente save(Cliente cliente);

    Optional<Cliente> findByClienteId(String clienteId);

    List<Cliente> findAll();

    boolean existsByClienteId(String clienteId);

    boolean existsByIdentificacion(String identificacion);

    void delete(Cliente cliente);
}

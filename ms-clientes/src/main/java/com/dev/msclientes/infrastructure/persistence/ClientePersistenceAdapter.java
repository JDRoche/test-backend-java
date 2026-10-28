package com.dev.msclientes.infrastructure.persistence;

import com.dev.msclientes.domain.model.Cliente;
import com.dev.msclientes.domain.port.out.ClienteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClientePersistenceAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpaRepository;
    private final ClienteMapper mapper;

    public ClientePersistenceAdapter(ClienteJpaRepository jpaRepository, ClienteMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Cliente save(Cliente cliente) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(cliente)));
    }

    @Override
    public Optional<Cliente> findByClienteId(String clienteId) {
        return jpaRepository.findByClienteId(clienteId).map(mapper::toDomain);
    }

    @Override
    public List<Cliente> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByClienteId(String clienteId) {
        return jpaRepository.existsByClienteId(clienteId);
    }

    @Override
    public boolean existsByIdentificacion(String identificacion) {
        return jpaRepository.existsByIdentificacion(identificacion);
    }

    @Override
    public void delete(Cliente cliente) {
        jpaRepository.deleteById(cliente.getId());
    }
}

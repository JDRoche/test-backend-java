package com.dev.msclientes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {

    Optional<ClienteJpaEntity> findByClienteId(String clienteId);

    boolean existsByClienteId(String clienteId);

    boolean existsByIdentificacion(String identificacion);
}

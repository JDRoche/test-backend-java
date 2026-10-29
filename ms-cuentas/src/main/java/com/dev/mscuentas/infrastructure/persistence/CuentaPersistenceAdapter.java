package com.dev.mscuentas.infrastructure.persistence;

import com.dev.mscuentas.domain.model.Cuenta;
import com.dev.mscuentas.domain.port.out.CuentaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CuentaPersistenceAdapter implements CuentaRepository {

    private final CuentaJpaRepository jpaRepository;
    private final CuentaMapper mapper;

    public CuentaPersistenceAdapter(CuentaJpaRepository jpaRepository, CuentaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Cuenta save(Cuenta cuenta) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(cuenta)));
    }

    @Override
    public Optional<Cuenta> findByNumeroCuenta(String numeroCuenta) {
        return jpaRepository.findByNumeroCuenta(numeroCuenta).map(mapper::toDomain);
    }

    @Override
    public Optional<Cuenta> findByNumeroCuentaForUpdate(String numeroCuenta) {
        return jpaRepository.findByNumeroCuentaForUpdate(numeroCuenta).map(mapper::toDomain);
    }

    @Override
    public List<Cuenta> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Cuenta> findByClienteId(String clienteId) {
        return jpaRepository.findByClienteId(clienteId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByNumeroCuenta(String numeroCuenta) {
        return jpaRepository.existsByNumeroCuenta(numeroCuenta);
    }
}

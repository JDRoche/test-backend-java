package com.dev.mscuentas.infrastructure.persistence;

import com.dev.mscuentas.domain.model.Movimiento;
import com.dev.mscuentas.domain.port.out.MovimientoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MovimientoPersistenceAdapter implements MovimientoRepository {

    private final MovimientoJpaRepository jpaRepository;
    private final CuentaJpaRepository cuentaJpaRepository;
    private final MovimientoMapper mapper;

    public MovimientoPersistenceAdapter(MovimientoJpaRepository jpaRepository,
                                        CuentaJpaRepository cuentaJpaRepository, MovimientoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.cuentaJpaRepository = cuentaJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Movimiento save(Movimiento movimiento) {
        CuentaJpaEntity cuenta = cuentaJpaRepository.getReferenceById(movimiento.getCuentaId());
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(movimiento, cuenta)));
    }

    @Override
    public Optional<Movimiento> findById(Long id) {
        return jpaRepository.findByIdWithCuenta(id).map(mapper::toDomain);
    }

    @Override
    public List<Movimiento> findAll() {
        return jpaRepository.findAllWithCuenta().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Movimiento> findByNumeroCuenta(String numeroCuenta) {
        return jpaRepository.findByNumeroCuentaWithCuenta(numeroCuenta).stream().map(mapper::toDomain).toList();
    }
}

package com.dev.mscuentas.domain.port.out;

import com.dev.mscuentas.domain.model.Movimiento;

import java.util.List;
import java.util.Optional;

public interface MovimientoRepository {

    Movimiento save(Movimiento movimiento);

    Optional<Movimiento> findById(Long id);

    /** Most recent first. */
    List<Movimiento> findAll();

    /** Most recent first. */
    List<Movimiento> findByNumeroCuenta(String numeroCuenta);
}

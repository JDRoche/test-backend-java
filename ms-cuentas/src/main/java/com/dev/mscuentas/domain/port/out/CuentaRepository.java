package com.dev.mscuentas.domain.port.out;

import com.dev.mscuentas.domain.model.Cuenta;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository {

    Cuenta save(Cuenta cuenta);

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    /** Loads the cuenta locking its row until the end of the transaction, so concurrent movements queue up. */
    Optional<Cuenta> findByNumeroCuentaForUpdate(String numeroCuenta);

    List<Cuenta> findAll();

    List<Cuenta> findByClienteId(String clienteId);

    boolean existsByNumeroCuenta(String numeroCuenta);
}

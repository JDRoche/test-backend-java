package com.dev.mscuentas.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuentaJpaRepository extends JpaRepository<CuentaJpaEntity, Long> {

    Optional<CuentaJpaEntity> findByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<CuentaJpaEntity> findByClienteId(String clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CuentaJpaEntity c where c.numeroCuenta = :numeroCuenta")
    Optional<CuentaJpaEntity> findByNumeroCuentaForUpdate(@Param("numeroCuenta") String numeroCuenta);
}

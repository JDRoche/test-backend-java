package com.dev.mscuentas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Every query fetches the cuenta in the same select to avoid N+1 when mapping the numeroCuenta. */
public interface MovimientoJpaRepository extends JpaRepository<MovimientoJpaEntity, Long> {

    @Query("select m from MovimientoJpaEntity m join fetch m.cuenta where m.id = :id")
    Optional<MovimientoJpaEntity> findByIdWithCuenta(@Param("id") Long id);

    @Query("select m from MovimientoJpaEntity m join fetch m.cuenta order by m.fecha desc, m.id desc")
    List<MovimientoJpaEntity> findAllWithCuenta();

    @Query("""
            select m from MovimientoJpaEntity m join fetch m.cuenta c
            where c.numeroCuenta = :numeroCuenta
            order by m.fecha desc, m.id desc""")
    List<MovimientoJpaEntity> findByNumeroCuentaWithCuenta(@Param("numeroCuenta") String numeroCuenta);

    /** One query for the whole statement: it uses the (cuenta_id, fecha) index of movimiento. */
    @Query("""
            select m from MovimientoJpaEntity m join fetch m.cuenta c
            where c.clienteId = :clienteId and m.fecha >= :rangeStart and m.fecha < :rangeEnd
            order by m.fecha desc, m.id desc""")
    List<MovimientoJpaEntity> findStatement(@Param("clienteId") String clienteId,
                                            @Param("rangeStart") LocalDateTime rangeStart,
                                            @Param("rangeEnd") LocalDateTime rangeEnd);
}

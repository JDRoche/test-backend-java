package com.dev.mscuentas.infrastructure.persistence;

import com.dev.mscuentas.domain.model.Movimiento;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapper {

    public Movimiento toDomain(MovimientoJpaEntity entity) {
        return Movimiento.rehydrate(entity.getId(), entity.getCuenta().getId(), entity.getCuenta().getNumeroCuenta(),
                entity.getFecha(), entity.getTipoMovimiento(), entity.getValor(), entity.getSaldo());
    }

    public MovimientoJpaEntity toEntity(Movimiento movimiento, CuentaJpaEntity cuenta) {
        MovimientoJpaEntity entity = new MovimientoJpaEntity();
        entity.setId(movimiento.getId());
        entity.setCuenta(cuenta);
        entity.setFecha(movimiento.getFecha());
        entity.setTipoMovimiento(movimiento.getTipoMovimiento());
        entity.setValor(movimiento.getValor());
        entity.setSaldo(movimiento.getSaldo());
        return entity;
    }
}

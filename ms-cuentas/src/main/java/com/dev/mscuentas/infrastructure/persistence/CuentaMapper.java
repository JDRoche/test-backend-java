package com.dev.mscuentas.infrastructure.persistence;

import com.dev.mscuentas.domain.model.Cuenta;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {

    public Cuenta toDomain(CuentaJpaEntity entity) {
        return Cuenta.rehydrate(entity.getId(), entity.getNumeroCuenta(), entity.getTipoCuenta(),
                entity.getSaldoInicial(), entity.getCurrentBalance(), entity.isEstado(), entity.getClienteId());
    }

    public CuentaJpaEntity toEntity(Cuenta cuenta) {
        CuentaJpaEntity entity = new CuentaJpaEntity();
        entity.setId(cuenta.getId());
        entity.setNumeroCuenta(cuenta.getNumeroCuenta());
        entity.setTipoCuenta(cuenta.getTipoCuenta());
        entity.setSaldoInicial(cuenta.getSaldoInicial());
        entity.setCurrentBalance(cuenta.getCurrentBalance());
        entity.setEstado(cuenta.isEstado());
        entity.setClienteId(cuenta.getClienteId());
        return entity;
    }
}

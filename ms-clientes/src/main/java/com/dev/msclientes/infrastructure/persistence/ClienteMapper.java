package com.dev.msclientes.infrastructure.persistence;

import com.dev.msclientes.domain.model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toDomain(ClienteJpaEntity entity) {
        return Cliente.rehydrate(entity.getId(), entity.getClienteId(), entity.getNombre(), entity.getGenero(),
                entity.getEdad(), entity.getIdentificacion(), entity.getDireccion(), entity.getTelefono(),
                entity.getContrasena(), entity.isEstado());
    }

    public ClienteJpaEntity toEntity(Cliente cliente) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setId(cliente.getId());
        entity.setClienteId(cliente.getClienteId());
        entity.setNombre(cliente.getNombre());
        entity.setGenero(cliente.getGenero());
        entity.setEdad(cliente.getEdad());
        entity.setIdentificacion(cliente.getIdentificacion());
        entity.setDireccion(cliente.getDireccion());
        entity.setTelefono(cliente.getTelefono());
        entity.setContrasena(cliente.getContrasena());
        entity.setEstado(cliente.isEstado());
        return entity;
    }
}

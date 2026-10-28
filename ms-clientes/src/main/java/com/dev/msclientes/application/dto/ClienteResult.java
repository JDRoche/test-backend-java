package com.dev.msclientes.application.dto;

import com.dev.msclientes.domain.model.Cliente;
import com.dev.msclientes.domain.model.Gender;

/** Output view of a cliente: never exposes the contrasena. */
public record ClienteResult(String clienteId, String nombre, Gender genero, Integer edad,
                            String identificacion, String direccion, String telefono, boolean estado) {

    public static ClienteResult from(Cliente cliente) {
        return new ClienteResult(cliente.getClienteId(), cliente.getNombre(), cliente.getGenero(),
                cliente.getEdad(), cliente.getIdentificacion(), cliente.getDireccion(),
                cliente.getTelefono(), cliente.isEstado());
    }
}

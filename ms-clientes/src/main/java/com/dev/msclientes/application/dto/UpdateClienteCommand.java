package com.dev.msclientes.application.dto;

import com.dev.msclientes.domain.model.Gender;

/** {@code contrasena} is optional: when null or blank the current one is kept. */
public record UpdateClienteCommand(String clienteId, String nombre, Gender genero, Integer edad,
                                   String identificacion, String direccion, String telefono,
                                   String contrasena, boolean estado) {
}

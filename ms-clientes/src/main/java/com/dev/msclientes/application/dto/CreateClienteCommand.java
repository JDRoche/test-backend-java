package com.dev.msclientes.application.dto;

import com.dev.msclientes.domain.model.Gender;

/** {@code clienteId} is optional: when omitted, the system generates one. */
public record CreateClienteCommand(String clienteId, String nombre, Gender genero, Integer edad,
                                   String identificacion, String direccion, String telefono,
                                   String contrasena) {
}

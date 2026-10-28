package com.dev.msclientes.presentation.request;

import com.dev.msclientes.application.dto.UpdateClienteCommand;
import com.dev.msclientes.domain.model.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateClienteRequest(
        @NotBlank(message = "es obligatorio") String nombre,
        Gender genero,
        @Min(value = 0, message = "debe ser mayor o igual a 0") @Max(value = 150, message = "debe ser menor o igual a 150") Integer edad,
        @NotBlank(message = "es obligatoria") String identificacion,
        String direccion,
        String telefono,
        String contrasena,
        @NotNull(message = "es obligatorio") Boolean estado) {

    public UpdateClienteCommand toCommand(String clienteId) {
        return new UpdateClienteCommand(clienteId, nombre, genero, edad, identificacion,
                direccion, telefono, contrasena, estado);
    }
}

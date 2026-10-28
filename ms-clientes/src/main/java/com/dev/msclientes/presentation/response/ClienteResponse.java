package com.dev.msclientes.presentation.response;

import com.dev.msclientes.application.dto.ClienteResult;
import com.dev.msclientes.domain.model.Gender;

public record ClienteResponse(String clienteId, String nombre, Gender genero, Integer edad,
                              String identificacion, String direccion, String telefono, boolean estado) {

    public static ClienteResponse from(ClienteResult result) {
        return new ClienteResponse(result.clienteId(), result.nombre(), result.genero(), result.edad(),
                result.identificacion(), result.direccion(), result.telefono(), result.estado());
    }
}

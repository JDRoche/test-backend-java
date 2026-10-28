package com.dev.msclientes.presentation.controller;

import com.dev.msclientes.application.dto.ClienteResult;
import com.dev.msclientes.domain.port.in.CreateClienteUseCase;
import com.dev.msclientes.domain.port.in.DeleteClienteUseCase;
import com.dev.msclientes.domain.port.in.QueryClienteUseCase;
import com.dev.msclientes.domain.port.in.UpdateClienteUseCase;
import com.dev.msclientes.presentation.request.CreateClienteRequest;
import com.dev.msclientes.presentation.request.UpdateClienteRequest;
import com.dev.msclientes.presentation.response.ClienteResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final CreateClienteUseCase createCliente;
    private final QueryClienteUseCase queryCliente;
    private final UpdateClienteUseCase updateCliente;
    private final DeleteClienteUseCase deleteCliente;

    public ClienteController(CreateClienteUseCase createCliente, QueryClienteUseCase queryCliente,
                             UpdateClienteUseCase updateCliente, DeleteClienteUseCase deleteCliente) {
        this.createCliente = createCliente;
        this.queryCliente = queryCliente;
        this.updateCliente = updateCliente;
        this.deleteCliente = deleteCliente;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> create(@Valid @RequestBody CreateClienteRequest request) {
        ClienteResult result = createCliente.create(request.toCommand());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{clienteId}").buildAndExpand(result.clienteId()).toUri();
        return ResponseEntity.created(location).body(ClienteResponse.from(result));
    }

    @GetMapping
    public List<ClienteResponse> list() {
        return queryCliente.list().stream().map(ClienteResponse::from).toList();
    }

    @GetMapping("/{clienteId}")
    public ClienteResponse getByClienteId(@PathVariable String clienteId) {
        return ClienteResponse.from(queryCliente.getByClienteId(clienteId));
    }

    @PutMapping("/{clienteId}")
    public ClienteResponse update(@PathVariable String clienteId, @Valid @RequestBody UpdateClienteRequest request) {
        return ClienteResponse.from(updateCliente.update(request.toCommand(clienteId)));
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> delete(@PathVariable String clienteId) {
        deleteCliente.delete(clienteId);
        return ResponseEntity.noContent().build();
    }
}

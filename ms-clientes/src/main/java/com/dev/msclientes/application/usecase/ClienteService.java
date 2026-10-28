package com.dev.msclientes.application.usecase;

import com.dev.msclientes.application.dto.ClienteResult;
import com.dev.msclientes.application.dto.CreateClienteCommand;
import com.dev.msclientes.application.dto.UpdateClienteCommand;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteCreated;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteDeleted;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteUpdated;
import com.dev.msclientes.domain.exception.DomainValidationException;
import com.dev.msclientes.domain.exception.DuplicateClienteException;
import com.dev.msclientes.domain.exception.ResourceNotFoundException;
import com.dev.msclientes.domain.model.Cliente;
import com.dev.msclientes.domain.port.in.CreateClienteUseCase;
import com.dev.msclientes.domain.port.in.DeleteClienteUseCase;
import com.dev.msclientes.domain.port.in.QueryClienteUseCase;
import com.dev.msclientes.domain.port.in.UpdateClienteUseCase;
import com.dev.msclientes.domain.port.out.ClienteEventPublisher;
import com.dev.msclientes.domain.port.out.ClienteRepository;
import com.dev.msclientes.domain.port.out.PasswordHasher;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
public class ClienteService implements CreateClienteUseCase, QueryClienteUseCase,
        UpdateClienteUseCase, DeleteClienteUseCase {

    private final ClienteRepository repository;
    private final ClienteEventPublisher eventPublisher;
    private final PasswordHasher passwordHasher;

    public ClienteService(ClienteRepository repository, ClienteEventPublisher eventPublisher,
                          PasswordHasher passwordHasher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public ClienteResult create(CreateClienteCommand command) {
        String clienteId = isBlank(command.clienteId()) ? UUID.randomUUID().toString() : command.clienteId().trim();
        if (isBlank(command.contrasena())) {
            throw new DomainValidationException("La contraseña es obligatoria");
        }
        if (repository.existsByClienteId(clienteId)) {
            throw new DuplicateClienteException("Ya existe un cliente con clienteId " + clienteId);
        }
        if (command.identificacion() != null && repository.existsByIdentificacion(command.identificacion().trim())) {
            throw new DuplicateClienteException("Ya existe un cliente con identificación " + command.identificacion());
        }

        Cliente cliente = Cliente.create(clienteId, command.nombre(), command.genero(), command.edad(),
                command.identificacion(), command.direccion(), command.telefono(),
                passwordHasher.hash(command.contrasena()));

        Cliente saved = repository.save(cliente);
        eventPublisher.publish(ClienteCreated.from(saved));
        return ClienteResult.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResult getByClienteId(String clienteId) {
        return ClienteResult.from(findOrThrow(clienteId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResult> list() {
        return repository.findAll().stream().map(ClienteResult::from).toList();
    }

    @Override
    public ClienteResult update(UpdateClienteCommand command) {
        Cliente cliente = findOrThrow(command.clienteId());

        if (command.identificacion() != null
                && !command.identificacion().trim().equals(cliente.getIdentificacion())
                && repository.existsByIdentificacion(command.identificacion().trim())) {
            throw new DuplicateClienteException("Ya existe un cliente con identificación " + command.identificacion());
        }

        cliente.update(command.nombre(), command.genero(), command.edad(), command.identificacion(),
                command.direccion(), command.telefono(), command.estado());
        if (!isBlank(command.contrasena())) {
            cliente.changePassword(passwordHasher.hash(command.contrasena()));
        }

        Cliente saved = repository.save(cliente);
        eventPublisher.publish(ClienteUpdated.from(saved));
        return ClienteResult.from(saved);
    }

    @Override
    public void delete(String clienteId) {
        Cliente cliente = findOrThrow(clienteId);
        repository.delete(cliente);
        eventPublisher.publish(ClienteDeleted.from(cliente));
    }

    private Cliente findOrThrow(String clienteId) {
        return repository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clienteId));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

package com.dev.msclientes.application.usecase;

import com.dev.msclientes.application.dto.ClienteResult;
import com.dev.msclientes.application.dto.CreateClienteCommand;
import com.dev.msclientes.application.dto.UpdateClienteCommand;
import com.dev.msclientes.domain.event.ClienteEvent;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteCreated;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteDeleted;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteUpdated;
import com.dev.msclientes.domain.exception.DomainValidationException;
import com.dev.msclientes.domain.exception.DuplicateClienteException;
import com.dev.msclientes.domain.exception.ResourceNotFoundException;
import com.dev.msclientes.domain.model.Cliente;
import com.dev.msclientes.domain.model.Gender;
import com.dev.msclientes.domain.port.out.ClienteEventPublisher;
import com.dev.msclientes.domain.port.out.ClienteRepository;
import com.dev.msclientes.domain.port.out.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClienteServiceTest {

    private final ClienteRepository repository = mock(ClienteRepository.class);
    private final ClienteEventPublisher publisher = mock(ClienteEventPublisher.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final ClienteService service = new ClienteService(repository, publisher, hasher);

    @BeforeEach
    void setUp() {
        when(hasher.hash(any())).thenAnswer(invocation -> "hash(" + invocation.getArgument(0) + ")");
        when(repository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private CreateClienteCommand createCommand(String clienteId, String identificacion) {
        return new CreateClienteCommand(clienteId, "Jose Lema", Gender.MALE, 35, identificacion,
                "Otavalo sn y principal", "098254785", "1234");
    }

    private Cliente existing(String clienteId, String identificacion) {
        return Cliente.rehydrate(1L, clienteId, "Jose Lema", Gender.MALE, 35, identificacion,
                "Otavalo sn y principal", "098254785", "hash(1234)", true);
    }

    private UpdateClienteCommand updateCommand(String clienteId, String identificacion, String contrasena) {
        return new UpdateClienteCommand(clienteId, "Jose Lema", Gender.MALE, 35, identificacion,
                "Otavalo sn y principal", "098254785", contrasena, true);
    }

    @Test
    @DisplayName("Create hashes the password, stores the cliente and publishes ClienteCreated")
    void createHashesPasswordAndPublishesEvent() {
        ClienteResult result = service.create(createCommand("jlema", "0100000001"));

        assertEquals("jlema", result.clienteId());
        assertTrue(result.estado());
        ArgumentCaptor<Cliente> saved = ArgumentCaptor.forClass(Cliente.class);
        verify(repository).save(saved.capture());
        assertEquals("hash(1234)", saved.getValue().getContrasena());
        ArgumentCaptor<ClienteEvent> event = ArgumentCaptor.forClass(ClienteEvent.class);
        verify(publisher).publish(event.capture());
        assertInstanceOf(ClienteCreated.class, event.getValue());
        assertEquals("jlema", event.getValue().clienteId());
    }

    @Test
    @DisplayName("A clienteId is generated when it is not sent")
    void generatesClienteId() {
        ClienteResult result = service.create(createCommand(null, "0100000001"));

        assertNotNull(result.clienteId());
        assertEquals(36, result.clienteId().length());
    }

    @Test
    @DisplayName("Create rejects a duplicated clienteId and a duplicated identificacion, publishing nothing")
    void createRejectsDuplicates() {
        when(repository.existsByClienteId("jlema")).thenReturn(true);
        assertThrows(DuplicateClienteException.class, () -> service.create(createCommand("jlema", "0100000001")));

        when(repository.existsByIdentificacion("0100000002")).thenReturn(true);
        assertThrows(DuplicateClienteException.class, () -> service.create(createCommand("otro", "0100000002")));

        verify(repository, never()).save(any());
        verify(publisher, never()).publish(any());
    }

    @Test
    @DisplayName("Create requires a password")
    void createRequiresPassword() {
        CreateClienteCommand noPassword = new CreateClienteCommand("jlema", "Jose Lema", Gender.MALE, 35,
                "0100000001", null, null, " ");

        assertThrows(DomainValidationException.class, () -> service.create(noPassword));
    }

    @Test
    @DisplayName("Update rejects the identificacion of another cliente")
    void updateRejectsIdentificacionOfAnotherCliente() {
        when(repository.findByClienteId("jlema")).thenReturn(Optional.of(existing("jlema", "0100000001")));
        when(repository.existsByIdentificacion("0100000002")).thenReturn(true);

        assertThrows(DuplicateClienteException.class,
                () -> service.update(updateCommand("jlema", "0100000002", null)));

        verify(repository, never()).save(any());
        verify(publisher, never()).publish(any());
    }

    @Test
    @DisplayName("Update accepts the cliente's own identificacion without checking duplicates")
    void updateAcceptsOwnIdentificacion() {
        when(repository.findByClienteId("jlema")).thenReturn(Optional.of(existing("jlema", "0100000001")));

        service.update(updateCommand("jlema", "0100000001", null));

        verify(repository, never()).existsByIdentificacion(any());
        ArgumentCaptor<ClienteEvent> event = ArgumentCaptor.forClass(ClienteEvent.class);
        verify(publisher).publish(event.capture());
        assertInstanceOf(ClienteUpdated.class, event.getValue());
    }

    @Test
    @DisplayName("Update keeps the current password when none is sent and changes it when one is")
    void updatePassword() {
        Cliente cliente = existing("jlema", "0100000001");
        when(repository.findByClienteId("jlema")).thenReturn(Optional.of(cliente));

        service.update(updateCommand("jlema", "0100000001", null));
        assertEquals("hash(1234)", cliente.getContrasena());

        service.update(updateCommand("jlema", "0100000001", "nueva"));
        assertEquals("hash(nueva)", cliente.getContrasena());
    }

    @Test
    @DisplayName("Update and delete of an unknown cliente are reported as not found")
    void unknownCliente() {
        when(repository.findByClienteId("nadie")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(updateCommand("nadie", "1", null)));
        assertThrows(ResourceNotFoundException.class, () -> service.delete("nadie"));
        assertThrows(ResourceNotFoundException.class, () -> service.getByClienteId("nadie"));
        verify(publisher, never()).publish(any());
    }

    @Test
    @DisplayName("Delete removes the cliente and publishes ClienteDeleted")
    void deletePublishesEvent() {
        Cliente cliente = existing("jlema", "0100000001");
        when(repository.findByClienteId("jlema")).thenReturn(Optional.of(cliente));

        service.delete("jlema");

        verify(repository).delete(cliente);
        ArgumentCaptor<ClienteEvent> event = ArgumentCaptor.forClass(ClienteEvent.class);
        verify(publisher).publish(event.capture());
        assertInstanceOf(ClienteDeleted.class, event.getValue());
    }
}

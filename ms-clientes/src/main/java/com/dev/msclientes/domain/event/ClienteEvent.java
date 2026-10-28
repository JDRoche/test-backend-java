package com.dev.msclientes.domain.event;

import com.dev.msclientes.domain.model.Cliente;

import java.time.Instant;
import java.util.UUID;

/** Domain events consumed by MS Cuentas. {@code eventId} enables idempotent consumers. */
public sealed interface ClienteEvent {

    UUID eventId();

    Instant occurredAt();

    String clienteId();

    record ClienteCreated(UUID eventId, Instant occurredAt, String clienteId,
                          String nombre, boolean estado) implements ClienteEvent {
        public static ClienteCreated from(Cliente cliente) {
            return new ClienteCreated(UUID.randomUUID(), Instant.now(),
                    cliente.getClienteId(), cliente.getNombre(), cliente.isEstado());
        }
    }

    record ClienteUpdated(UUID eventId, Instant occurredAt, String clienteId,
                          String nombre, boolean estado) implements ClienteEvent {
        public static ClienteUpdated from(Cliente cliente) {
            return new ClienteUpdated(UUID.randomUUID(), Instant.now(),
                    cliente.getClienteId(), cliente.getNombre(), cliente.isEstado());
        }
    }

    record ClienteDeleted(UUID eventId, Instant occurredAt, String clienteId) implements ClienteEvent {
        public static ClienteDeleted from(Cliente cliente) {
            return new ClienteDeleted(UUID.randomUUID(), Instant.now(), cliente.getClienteId());
        }
    }
}

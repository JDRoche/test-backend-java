package com.dev.mscuentas.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Change of a cliente reported by MS Clientes. {@code nombre} is null when the cliente was deleted.
 */
public record SyncClienteCommand(UUID eventId, Instant occurredAt, String clienteId,
                                 String nombre, boolean estado, boolean deleted) {

    public static SyncClienteCommand upsert(UUID eventId, Instant occurredAt, String clienteId,
                                            String nombre, boolean estado) {
        return new SyncClienteCommand(eventId, occurredAt, clienteId, nombre, estado, false);
    }

    public static SyncClienteCommand deletion(UUID eventId, Instant occurredAt, String clienteId) {
        return new SyncClienteCommand(eventId, occurredAt, clienteId, null, false, true);
    }
}

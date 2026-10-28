package com.dev.mscuentas.domain.model;

import java.time.Instant;

/**
 * Minimal local copy of a cliente owned by MS Clientes, kept up to date through its events.
 * Events can arrive late or out of order, so a change older than the last applied one is ignored.
 */
public class ClienteSnapshot {

    private final String clienteId;
    private String nombre;
    private boolean estado;
    private Instant lastEventAt;

    private ClienteSnapshot(String clienteId, String nombre, boolean estado, Instant lastEventAt) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.estado = estado;
        this.lastEventAt = lastEventAt;
    }

    public static ClienteSnapshot create(String clienteId, String nombre, boolean estado, Instant occurredAt) {
        return new ClienteSnapshot(clienteId, nombre, estado, occurredAt);
    }

    public static ClienteSnapshot rehydrate(String clienteId, String nombre, boolean estado, Instant lastEventAt) {
        return new ClienteSnapshot(clienteId, nombre, estado, lastEventAt);
    }

    /** @return true when the change was applied, false when it was stale */
    public boolean update(String nombre, boolean estado, Instant occurredAt) {
        if (isStale(occurredAt)) {
            return false;
        }
        this.nombre = nombre;
        this.estado = estado;
        this.lastEventAt = occurredAt;
        return true;
    }

    /** A deleted cliente is kept as inactive so a late "created" event cannot bring it back. */
    public boolean markDeleted(Instant occurredAt) {
        if (isStale(occurredAt)) {
            return false;
        }
        this.estado = false;
        this.lastEventAt = occurredAt;
        return true;
    }

    private boolean isStale(Instant occurredAt) {
        return occurredAt.isBefore(lastEventAt);
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isEstado() {
        return estado;
    }

    public Instant getLastEventAt() {
        return lastEventAt;
    }
}

package com.dev.mscuentas.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire format of the cliente events published by MS Clientes. It is this service's own copy of the
 * contract: {@code nombre} and {@code estado} are absent in "deleted" events.
 */
public record ClienteEventMessage(UUID eventId, Instant occurredAt, String clienteId,
                                  String nombre, Boolean estado) {
}

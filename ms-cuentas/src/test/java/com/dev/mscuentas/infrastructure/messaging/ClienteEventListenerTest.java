package com.dev.mscuentas.infrastructure.messaging;

import com.dev.mscuentas.application.dto.SyncClienteCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteEventListenerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final Instant AT = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    @DisplayName("created and updated become an upsert command")
    void mapsCreatedToUpsert() {
        SyncClienteCommand command = ClienteEventListener.toCommand("cliente.created",
                new ClienteEventMessage(EVENT_ID, AT, "jlema", "Jose Lema", true));

        assertFalse(command.deleted());
        assertEquals("Jose Lema", command.nombre());
        assertTrue(command.estado());
    }

    @Test
    @DisplayName("deleted becomes a deletion command without nombre")
    void mapsDeletedToDeletion() {
        SyncClienteCommand command = ClienteEventListener.toCommand("cliente.deleted",
                new ClienteEventMessage(EVENT_ID, AT, "jlema", null, null));

        assertTrue(command.deleted());
        assertNull(command.nombre());
    }

    @Test
    @DisplayName("A message without eventId is rejected without requeue (goes to the DLQ)")
    void rejectsMessageWithoutEventId() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> ClienteEventListener.toCommand("cliente.created",
                new ClienteEventMessage(null, AT, "jlema", "Jose Lema", true)));
    }

    @Test
    @DisplayName("A created event without nombre is rejected without requeue")
    void rejectsCreatedWithoutNombre() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> ClienteEventListener.toCommand("cliente.created",
                new ClienteEventMessage(EVENT_ID, AT, "jlema", null, true)));
    }

    @Test
    @DisplayName("An unknown routing key is rejected without requeue")
    void rejectsUnknownRoutingKey() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> ClienteEventListener.toCommand("cliente.other",
                new ClienteEventMessage(EVENT_ID, AT, "jlema", "Jose Lema", true)));
    }
}

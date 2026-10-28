package com.dev.msclientes.infrastructure.messaging;

import com.dev.msclientes.domain.event.ClienteEvent;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteCreated;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteDeleted;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteUpdated;
import com.dev.msclientes.infrastructure.config.RabbitConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ClienteEventRelayTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final ClienteEventRelay relay = new ClienteEventRelay(rabbitTemplate);

    private static ClienteCreated created() {
        return new ClienteCreated(UUID.randomUUID(), Instant.now(), "jlema", "Jose Lema", true);
    }

    @Test
    @DisplayName("Each event type maps to its own routing key")
    void mapsEachEventToRoutingKey() {
        assertEquals("cliente.created", ClienteEventRelay.routingKeyFor(created()));
        assertEquals("cliente.updated", ClienteEventRelay.routingKeyFor(
                new ClienteUpdated(UUID.randomUUID(), Instant.now(), "jlema", "Jose Lema", false)));
        assertEquals("cliente.deleted", ClienteEventRelay.routingKeyFor(
                new ClienteDeleted(UUID.randomUUID(), Instant.now(), "jlema")));
    }

    @Test
    @DisplayName("The event is sent to the cliente exchange with its routing key")
    void sendsEventToExchange() {
        ClienteEvent event = created();

        relay.onClienteEvent(event);

        verify(rabbitTemplate).convertAndSend(RabbitConfig.CLIENTE_EVENTS_EXCHANGE, "cliente.created", event);
    }

    @Test
    @DisplayName("A broker failure is logged and does not propagate to the caller")
    void swallowsBrokerFailure() {
        doThrow(new AmqpConnectException(new IOException("broker down")))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertDoesNotThrow(() -> relay.onClienteEvent(created()));
    }
}

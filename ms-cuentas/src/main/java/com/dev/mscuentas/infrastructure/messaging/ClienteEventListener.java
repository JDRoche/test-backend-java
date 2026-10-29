package com.dev.mscuentas.infrastructure.messaging;

import com.dev.mscuentas.application.dto.SyncClienteCommand;
import com.dev.mscuentas.domain.port.in.SyncClienteUseCase;
import com.dev.mscuentas.infrastructure.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class ClienteEventListener {

    private static final Logger log = LoggerFactory.getLogger(ClienteEventListener.class);

    static final String CREATED_KEY = "cliente.created";
    static final String UPDATED_KEY = "cliente.updated";
    static final String DELETED_KEY = "cliente.deleted";

    private final SyncClienteUseCase syncCliente;

    public ClienteEventListener(SyncClienteUseCase syncCliente) {
        this.syncCliente = syncCliente;
    }

    @RabbitListener(queues = RabbitConfig.CLIENTE_EVENTS_QUEUE)
    public void onClienteEvent(ClienteEventMessage message,
                               @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        log.info("Received {} for clienteId={} eventId={}", routingKey, message.clienteId(), message.eventId());
        syncCliente.sync(toCommand(routingKey, message));
    }

    static SyncClienteCommand toCommand(String routingKey, ClienteEventMessage message) {
        if (message.eventId() == null || message.occurredAt() == null || message.clienteId() == null) {
            throw new AmqpRejectAndDontRequeueException("Malformed cliente event: missing eventId, occurredAt or clienteId");
        }
        return switch (routingKey) {
            case CREATED_KEY, UPDATED_KEY -> {
                if (message.nombre() == null || message.estado() == null) {
                    throw new AmqpRejectAndDontRequeueException("Malformed " + routingKey + ": missing nombre or estado");
                }
                yield SyncClienteCommand.upsert(message.eventId(), message.occurredAt(), message.clienteId(),
                        message.nombre(), message.estado());
            }
            case DELETED_KEY -> SyncClienteCommand.deletion(message.eventId(), message.occurredAt(), message.clienteId());
            default -> throw new AmqpRejectAndDontRequeueException("Unknown routing key: " + routingKey);
        };
    }
}

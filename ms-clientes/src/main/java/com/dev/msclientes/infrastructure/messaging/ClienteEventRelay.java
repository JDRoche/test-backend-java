package com.dev.msclientes.infrastructure.messaging;

import com.dev.msclientes.domain.event.ClienteEvent;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteCreated;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteDeleted;
import com.dev.msclientes.domain.event.ClienteEvent.ClienteUpdated;
import com.dev.msclientes.infrastructure.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends domain events to RabbitMQ only after the database transaction has committed, so a rolled-back
 * change never produces a "ghost" event. If the broker is down the event is lost and logged: closing
 * that gap requires the Outbox pattern.
 */
@Component
public class ClienteEventRelay {

    private static final Logger log = LoggerFactory.getLogger(ClienteEventRelay.class);

    static final String CREATED_KEY = "cliente.created";
    static final String UPDATED_KEY = "cliente.updated";
    static final String DELETED_KEY = "cliente.deleted";

    private final RabbitTemplate rabbitTemplate;

    public ClienteEventRelay(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onClienteEvent(ClienteEvent event) {
        String routingKey = routingKeyFor(event);
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.CLIENTE_EVENTS_EXCHANGE, routingKey, event);
            log.info("Published {} for clienteId={}", routingKey, event.clienteId());
        } catch (AmqpException ex) {
            log.error("Could not publish {} for clienteId={}; event lost", routingKey, event.clienteId(), ex);
        }
    }

    static String routingKeyFor(ClienteEvent event) {
        return switch (event) {
            case ClienteCreated created -> CREATED_KEY;
            case ClienteUpdated updated -> UPDATED_KEY;
            case ClienteDeleted deleted -> DELETED_KEY;
        };
    }
}

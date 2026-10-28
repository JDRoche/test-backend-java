package com.dev.msclientes.infrastructure.messaging;

import com.dev.msclientes.domain.event.ClienteEvent;
import com.dev.msclientes.domain.port.out.ClienteEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter of the domain port. It does not talk to the broker: it hands the event to Spring,
 * and {@link ClienteEventRelay} forwards it to RabbitMQ once the transaction has committed.
 */
@Component
public class ApplicationClienteEventPublisher implements ClienteEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public ApplicationClienteEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(ClienteEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

package com.dev.msclientes.domain.port.out;

import com.dev.msclientes.domain.event.ClienteEvent;

public interface ClienteEventPublisher {

    void publish(ClienteEvent event);
}

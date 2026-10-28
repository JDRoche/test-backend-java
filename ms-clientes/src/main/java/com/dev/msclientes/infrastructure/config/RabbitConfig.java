package com.dev.msclientes.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MS Clientes only owns the exchange. Each consumer declares its own queue, bindings and DLQ,
 * so the producer does not need to know who listens.
 */
@Configuration
public class RabbitConfig {

    public static final String CLIENTE_EVENTS_EXCHANGE = "cliente.events";

    @Bean
    public TopicExchange clienteEventsExchange() {
        return new TopicExchange(CLIENTE_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

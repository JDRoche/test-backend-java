package com.dev.mscuentas.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper.TypePrecedence;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Consumer-side topology. The exchange is declared here too (the declaration is idempotent) so the
 * binding never depends on MS Clientes having started first. Messages that exhaust their retries
 * are dead-lettered to the DLQ instead of being lost or looping forever.
 */
@Configuration
public class RabbitConfig {

    public static final String CLIENTE_EVENTS_EXCHANGE = "cliente.events";
    public static final String CLIENTE_EVENTS_QUEUE = "cuentas.cliente-events";
    public static final String DEAD_LETTER_EXCHANGE = "cuentas.cliente-events.dlx";
    public static final String DEAD_LETTER_QUEUE = "cuentas.cliente-events.dlq";

    @Bean
    public TopicExchange clienteEventsExchange() {
        return new TopicExchange(CLIENTE_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue clienteEventsQueue() {
        return QueueBuilder.durable(CLIENTE_EVENTS_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding clienteEventsBinding() {
        return BindingBuilder.bind(clienteEventsQueue()).to(clienteEventsExchange()).with("cliente.*");
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_LETTER_QUEUE);
    }

    /** Uses the listener's parameter type, so this service does not need the producer's classes. */
    @Bean
    public MessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTypePrecedence(TypePrecedence.INFERRED);
        return converter;
    }
}

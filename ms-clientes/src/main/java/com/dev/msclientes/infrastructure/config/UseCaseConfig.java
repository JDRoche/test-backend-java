package com.dev.msclientes.infrastructure.config;

import com.dev.msclientes.application.usecase.ClienteService;
import com.dev.msclientes.domain.port.out.ClienteEventPublisher;
import com.dev.msclientes.domain.port.out.ClienteRepository;
import com.dev.msclientes.domain.port.out.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the use cases so that the application layer carries no stereotype annotations. */
@Configuration
public class UseCaseConfig {

    @Bean
    public ClienteService clienteService(ClienteRepository repository, ClienteEventPublisher eventPublisher,
                                         PasswordHasher passwordHasher) {
        return new ClienteService(repository, eventPublisher, passwordHasher);
    }
}

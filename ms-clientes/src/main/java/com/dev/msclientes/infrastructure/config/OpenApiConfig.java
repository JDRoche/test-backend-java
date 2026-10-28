package com.dev.msclientes.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msClientesOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MS Clientes")
                .description("Gestión de clientes (CRUD). Publica eventos a RabbitMQ.")
                .version("v1"));
    }
}

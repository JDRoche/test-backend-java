package com.dev.mscuentas.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msCuentasOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MS Cuentas")
                .description("Gestión de cuentas y movimientos. Consume los eventos de MS Clientes.")
                .version("v1"));
    }
}

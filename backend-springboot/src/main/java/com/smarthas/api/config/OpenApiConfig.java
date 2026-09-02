package com.smarthas.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configura o Swagger/OpenAPI, incluindo o esquema de autenticacao Bearer (JWT). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartHasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart HAS API")
                        .version("1.0.0")
                        .description("API REST do Smart HAS - monitoramento de Hipertensao Arterial Sistemica."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

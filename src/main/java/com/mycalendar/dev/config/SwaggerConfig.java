package com.mycalendar.dev.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class SwaggerConfig {
    @Bean
    public OpenAPI swaggerOpenAPI() {
        Info info = new Info()
                .title("API Document")
                .description("Swagger UI")
                .version("3.0");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerToken");

        SecurityScheme securityScheme = new SecurityScheme()
                .name("bearerToken")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("bearerToken", securityScheme));
    }
}
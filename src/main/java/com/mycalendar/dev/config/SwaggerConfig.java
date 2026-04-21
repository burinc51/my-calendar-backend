package com.mycalendar.dev.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

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

        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("bearerToken", securityScheme));

        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String normalizedBaseUrl = publicBaseUrl.trim().replaceAll("/$", "");
            openAPI.servers(List.of(new Server().url(normalizedBaseUrl)));
        }

        return openAPI;
    }
}
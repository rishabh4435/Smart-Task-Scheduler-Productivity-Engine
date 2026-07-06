package com.thakur.scheduler.config;


import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

        @Bean
        public OpenAPI schedulerApi() {
                return new OpenAPI()
                        .info(new Info()
                                .title("Smart Task Scheduler API")
                                .version("1.0.0")
                                .description("""
                                REST API for Smart Task Scheduler

                                Features
                                - JWT Authentication
                                - Task Management
                                - Dependency Graph
                                - Scheduling Engine
                                - Recommendation Engine
                                - Admin Panel
                                """)
                                .contact(new Contact()
                                        .name("Rishabh Thakur")
                                        .email("your@email.com"))
                                .license(new License().name("MIT")));
        }
}
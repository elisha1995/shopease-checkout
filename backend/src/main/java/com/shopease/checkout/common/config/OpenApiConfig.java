package com.shopease.checkout.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI shopEaseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShopEase Checkout API")
                        .description("""
                                Mini e-commerce checkout system demonstrating design patterns:
                                Strategy, Factory, Adapter, Observer, and Decorator.

                                Register a new account to get started. Available tiers: STANDARD, GOLD.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Elisha Samuel Gyamfi")));
    }
}

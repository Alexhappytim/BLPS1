package com.blps.app.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blpsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BLPS API")
                        .description("API for learning platform business processes")
                        .version("v1")
                        .contact(new Contact().name("BLPS Team"))
                        .license(new License().name("Proprietary")));
    }
}

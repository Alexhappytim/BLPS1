package com.blps.app.infrastructure.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

@Configuration
public class CrmClientConfig {

    @Bean
    public Client crmJaxRsClient(ObjectMapper objectMapper) {
        ClientConfig clientConfig = new ClientConfig();

        JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
        jacksonProvider.setMapper(objectMapper);

        return ClientBuilder.newClient(clientConfig)
                .register(jacksonProvider);
    }

    @Bean
    public String crmBaseUrl(@Value("${app.crm.base-url:http://localhost:1313/BLPS/hs/skillbox}") String baseUrl) {
        return baseUrl;
    }
}

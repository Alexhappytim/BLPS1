package com.blps.app.infrastructure.crm.jca;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

/**
 * Spring configuration that bootstraps the JCA adapter for the Skillbox CRM.
 *
 * Wiring order:
 *   JAX-RS Client (shared, thread-safe)
 *     └── CrmManagedConnectionFactory  (holds config + creates ManagedConnections)
 *           └── CrmConnectionFactory   (Spring bean — injected into services)
 *                 └── CrmSimpleConnectionManager  (allocates connections)
 */
@Configuration
public class CrmJcaConfig {

    /**
     * Shared, thread-safe JAX-RS client reused across all CRM connections.
     * Marked with destroyMethod="close" so Spring closes it on shutdown.
     */
    @Bean(destroyMethod = "close")
    public Client crmJaxRsClient(ObjectMapper objectMapper) {
        ClientConfig clientConfig = new ClientConfig();
        JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
        jacksonProvider.setMapper(objectMapper);
        return ClientBuilder.newClient(clientConfig).register(jacksonProvider);
    }

    /**
     * JCA ManagedConnectionFactory — holds the base URL and creates physical connections.
     */
    @Bean
    public CrmManagedConnectionFactory crmManagedConnectionFactory(
            Client crmJaxRsClient,
            @Value("${app.crm.base-url:http://localhost:1313/BLPS/hs/skillbox}") String baseUrl) {
        return new CrmManagedConnectionFactory(crmJaxRsClient, baseUrl);
    }

    /**
     * JCA ConnectionFactory — the bean injected into service code.
     * Uses a simple (non-pooling) ConnectionManager suitable for standalone Spring Boot.
     */
    @Bean
    public CrmConnectionFactory crmConnectionFactory(CrmManagedConnectionFactory mcf) {
        return new CrmConnectionFactoryImpl(mcf, new CrmSimpleConnectionManager());
    }
}

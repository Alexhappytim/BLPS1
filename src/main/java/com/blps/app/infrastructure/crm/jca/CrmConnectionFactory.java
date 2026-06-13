package com.blps.app.infrastructure.crm.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionFactory;

/**
 * JCA ConnectionFactory for the CRM system.
 * Registered as a Spring bean and injected into services.
 * Implements the jakarta.resource.cci.ConnectionFactory SPI contract.
 */
public interface CrmConnectionFactory extends ConnectionFactory {

    /**
     * Obtain a CRM connection handle.
     * Always use in try-with-resources to ensure the handle is closed.
     */
    CrmConnection getConnection() throws ResourceException;
}

package com.blps.app.infrastructure.crm.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;

/**
 * Simple (non-pooling) JCA ConnectionManager.
 * Creates a new {@link CrmManagedConnection} on every {@link #allocateConnection} call.
 *
 * This is the standard approach for a standalone Spring Boot application
 * where no JEE container manages connection pooling.
 * For production use at scale, replace with a pooling ConnectionManager.
 */
public class CrmSimpleConnectionManager implements ConnectionManager {

    @Override
    public Object allocateConnection(ManagedConnectionFactory mcf,
                                     ConnectionRequestInfo cri) throws ResourceException {
        // Create a new physical connection (ManagedConnection)
        ManagedConnection mc = mcf.createManagedConnection(null, cri);
        // Return the logical application-level handle from it
        return mc.getConnection(null, cri);
    }
}

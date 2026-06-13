package com.blps.app.infrastructure.crm.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.ws.rs.client.Client;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Set;

/**
 * JCA ManagedConnectionFactory for the CRM system.
 * Holds configuration (base URL) and creates {@link CrmManagedConnection} instances.
 * Registered as a Spring bean; passed to the {@link CrmSimpleConnectionManager}.
 */
@ConnectionDefinition(
        connectionFactory = CrmConnectionFactory.class,
        connectionFactoryImpl = CrmConnectionFactoryImpl.class,
        connection = CrmConnection.class,
        connectionImpl = CrmConnectionImpl.class
)
public class CrmManagedConnectionFactory implements ManagedConnectionFactory {

    private final Client client;
    private final String baseUrl;
    private PrintWriter logWriter;

    public CrmManagedConnectionFactory(Client client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    /**
     * Creates a new physical connection to the CRM.
     * Called by the {@link CrmSimpleConnectionManager} on every {@code getConnection()} call.
     */
    @Override
    public Object createConnectionFactory(ConnectionManager cm) throws ResourceException {
        return new CrmConnectionFactoryImpl(this, cm);
    }

    /**
     * Creates a standalone connection factory without a container-managed ConnectionManager.
     * Used when wiring manually in Spring (not inside a JEE container).
     */
    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new CrmConnectionFactoryImpl(this, new CrmSimpleConnectionManager());
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject,
                                                     ConnectionRequestInfo cri) {
        return new CrmManagedConnection(client, baseUrl);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ManagedConnection matchManagedConnections(Set connectionSet,
                                                     Subject subject,
                                                     ConnectionRequestInfo cri) {
        // No pooling — return first available connection (or null to signal "create new").
        if (connectionSet != null && !connectionSet.isEmpty()) {
            return (ManagedConnection) connectionSet.iterator().next();
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }
}

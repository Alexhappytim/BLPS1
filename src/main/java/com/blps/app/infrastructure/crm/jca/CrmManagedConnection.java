package com.blps.app.infrastructure.crm.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.ws.rs.client.Client;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JCA ManagedConnection — represents a single physical connection to the CRM.
 * Holds the shared JAX-RS Client and creates logical CrmConnection handles
 * on demand via {@link #getConnection}.
 *
 * In this implementation the JAX-RS Client is shared and stateless,
 * so there is no actual "physical connection" to manage — the managed
 * connection is effectively just a holder for the configuration.
 */
public class CrmManagedConnection implements ManagedConnection {

    private final Client client;
    private final String baseUrl;
    private final List<ConnectionEventListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private PrintWriter logWriter;

    public CrmManagedConnection(Client client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the application-level connection handle (what service code uses).
     */
    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cri) {
        return new CrmConnectionImpl(client, baseUrl);
    }

    @Override
    public void destroy() {
        // The JAX-RS Client lifecycle is managed by Spring — do not close it here.
    }

    @Override
    public void cleanup() {
        // Nothing to clean up for a stateless REST client.
    }

    @Override
    public void associateConnection(Object connection) {
        // Connection re-association not needed for simple non-pooled usage.
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() {
        // No XA (distributed transaction) support for a REST endpoint.
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() {
        // No local transaction support for a REST endpoint.
        return null;
    }

    @Override
    public ManagedConnectionMetaData getMetaData() {
        return new ManagedConnectionMetaData() {
            @Override public String getEISProductName() { return "Skillbox CRM"; }
            @Override public String getEISProductVersion() { return "1.0"; }
            @Override public int getMaxConnections() { return 0; }
            @Override public String getUserName() { return ""; }
        };
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

package com.blps.app.infrastructure.crm.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import javax.transaction.xa.XAResource;
import java.io.Serializable;

/**
 * JCA ResourceAdapter lifecycle class (required by the JCA SPI).
 * In a standalone Spring Boot application this class is not bootstrapped
 * by a JEE container, so all lifecycle methods are no-ops.
 * Its presence satisfies the JCA specification contract declared via
 * {@link CrmManagedConnectionFactory#@Connector}.
 */
@Connector(
        displayName = "Skillbox CRM Resource Adapter",
        vendorName = "BLPS",
        version = "1.0",
        eisType = "REST",
        transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction
)
public class CrmResourceAdapter implements ResourceAdapter, Serializable {

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        // No-op: lifecycle managed by Spring, not by a JEE container
    }

    @Override
    public void stop() {
        // No-op
    }

    @Override
    public void endpointActivation(MessageEndpointFactory mef, ActivationSpec spec) {
        // No inbound messaging support
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec spec) {
        // No inbound messaging support
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) {
        // No XA support for REST
        return new XAResource[0];
    }
}

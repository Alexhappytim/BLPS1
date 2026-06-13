package com.blps.app.infrastructure.crm.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.RecordFactory;
import jakarta.resource.cci.ResourceAdapterMetaData;
import jakarta.resource.spi.ConnectionManager;

import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * JCA ConnectionFactory implementation.
 * Registered as a Spring bean via {@link CrmJcaConfig}.
 * Service code injects this and calls {@link #getConnection()} to get a {@link CrmConnection}.
 */
public class CrmConnectionFactoryImpl implements CrmConnectionFactory {

    private final CrmManagedConnectionFactory mcf;
    private final ConnectionManager cm;
    private Reference reference;

    public CrmConnectionFactoryImpl(CrmManagedConnectionFactory mcf, ConnectionManager cm) {
        this.mcf = mcf;
        this.cm = cm;
    }

    /**
     * Obtain a CRM connection handle.
     * Always use in try-with-resources.
     */
    @Override
    public CrmConnection getConnection() throws ResourceException {
        return (CrmConnection) cm.allocateConnection(mcf, null);
    }

    // ── jakarta.resource.cci.ConnectionFactory boilerplate ───────────────────

    @Override
    public CrmConnection getConnection(ConnectionSpec properties) throws ResourceException {
        return getConnection();
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return new ResourceAdapterMetaData() {
            @Override public String getAdapterVersion() { return "1.0"; }
            @Override public String getAdapterVendorName() { return "BLPS"; }
            @Override public String getAdapterName() { return "Skillbox CRM Adapter"; }
            @Override public String getAdapterShortDescription() { return "JCA adapter for Skillbox CRM REST API"; }
            @Override public String getSpecVersion() { return "2.1"; }
            @Override public String[] getInteractionSpecsSupported() { return new String[0]; }
            @Override public boolean supportsExecuteWithInputAndOutputRecord() { return false; }
            @Override public boolean supportsExecuteWithInputRecordOnly() { return false; }
            @Override public boolean supportsLocalTransactionDemarcation() { return false; }
        };
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        throw new ResourceException("RecordFactory not supported by the CRM adapter");
    }

    // ── javax.naming.Referenceable (required by JCA SPI) ─────────────────────

    @Override
    public Reference getReference() throws NamingException {
        return reference;
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }
}

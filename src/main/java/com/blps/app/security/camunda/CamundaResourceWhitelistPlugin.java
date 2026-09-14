package com.blps.app.security.camunda;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.stereotype.Component;

@Component
public class CamundaResourceWhitelistPlugin extends AbstractProcessEnginePlugin {

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        String pattern = "[a-zA-Z0-9._@/\\-]+";
        processEngineConfiguration.setGeneralResourceWhitelistPattern(pattern);
        processEngineConfiguration.setGroupResourceWhitelistPattern(pattern);
        processEngineConfiguration.setUserResourceWhitelistPattern(pattern);
    }
}

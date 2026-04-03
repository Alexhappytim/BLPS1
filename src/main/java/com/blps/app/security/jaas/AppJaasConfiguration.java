package com.blps.app.security.jaas;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.Map;

public class AppJaasConfiguration extends Configuration {

    private final AppConfigurationEntry[] entries = new AppConfigurationEntry[] {
            new AppConfigurationEntry(
                    DbBackedLoginModule.class.getName(),
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    Map.of()
            )
    };

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return entries;
    }
}

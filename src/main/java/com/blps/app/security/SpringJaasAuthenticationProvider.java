package com.blps.app.security;

import org.springframework.security.authentication.jaas.AbstractJaasAuthenticationProvider;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class SpringJaasAuthenticationProvider extends AbstractJaasAuthenticationProvider {

    private final Configuration configuration;
    private String loginContextName = "blps";

    public SpringJaasAuthenticationProvider(Configuration configuration) {
        this.configuration = configuration;
        super.setLoginContextName(this.loginContextName);
    }

    @Override
    public void setLoginContextName(String loginContextName) {
        super.setLoginContextName(loginContextName);
        this.loginContextName = loginContextName;
    }

    @Override
    protected LoginContext createLoginContext(CallbackHandler handler) throws LoginException {
        return new LoginContext(loginContextName, null, handler, configuration);
    }
}

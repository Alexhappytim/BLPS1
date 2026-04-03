package com.blps.app.security.jaas;

import com.blps.app.common.BusinessException;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class DbBackedLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private boolean authenticated;
    private AppUser authenticatedUser;

    @Override
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map<String, ?> sharedState,
                           Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        try {
            NameCallback nameCallback = new NameCallback("username");
            PasswordCallback passwordCallback = new PasswordCallback("password", false);
            callbackHandler.handle(new Callback[] { nameCallback, passwordCallback });

            String username = nameCallback.getName();
            char[] passwordChars = passwordCallback.getPassword();
            String password = passwordChars == null ? "" : new String(passwordChars);
            passwordCallback.clearPassword();

            AppUserRepository appUserRepository = SpringContext.getBean(AppUserRepository.class);
            PasswordEncoder passwordEncoder = SpringContext.getBean(PasswordEncoder.class);

            AppUser user = appUserRepository.findByLogin(username)
                    .orElseThrow(() -> new BusinessException("Invalid credentials"));

            if (!user.isEnabled() || !user.isEmailVerified()) {
                throw new BusinessException("User account is not active");
            }

            if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new BusinessException("Invalid credentials");
            }

            authenticatedUser = user;
            authenticated = true;
            return true;
        } catch (IOException | UnsupportedCallbackException ex) {
            throw new LoginException("JAAS callback failed: " + ex.getMessage());
        } catch (BusinessException ex) {
            LoginException loginException = new LoginException(ex.getMessage());
            loginException.initCause(ex);
            throw loginException;
        }
    }

    @Override
    public boolean commit() {
        if (!authenticated || authenticatedUser == null) {
            return false;
        }

        subject.getPrincipals().add(new UserPrincipal(authenticatedUser.getLogin()));
        subject.getPrincipals().add(new RolePrincipal("ROLE_" + authenticatedUser.getRole().name()));
        return true;
    }

    @Override
    public boolean abort() {
        authenticated = false;
        authenticatedUser = null;
        return true;
    }

    @Override
    public boolean logout() {
        if (authenticatedUser != null) {
            subject.getPrincipals().remove(new UserPrincipal(authenticatedUser.getLogin()));
            subject.getPrincipals().remove(new RolePrincipal("ROLE_" + authenticatedUser.getRole().name()));
        }
        authenticated = false;
        authenticatedUser = null;
        return true;
    }
}

package com.blps.app.security.jaas;

import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Set;

@Component
public class RolePrincipalAuthorityGranter implements AuthorityGranter {

    @Override
    public Set<String> grant(Principal principal) {
        if (principal instanceof RolePrincipal rolePrincipal) {
            return Set.of(rolePrincipal.getName());
        }
        return Set.of();
    }
}

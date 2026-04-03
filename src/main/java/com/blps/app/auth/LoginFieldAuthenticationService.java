package com.blps.app.auth;

import com.blps.app.common.BusinessException;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginFieldAuthenticationService implements AuthenticationService {

    private final AppUserRepository appUserRepository;

    public LoginFieldAuthenticationService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional
    public AuthenticatedUser authenticateByLogin(String login) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("User is not authenticated");
        }

        if (!authentication.getName().equalsIgnoreCase(login)) {
            throw new BusinessException("Authenticated user does not match login field");
        }

        AppUser user = appUserRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new BusinessException("User is not found"));

        if (!user.isEnabled() || !user.isEmailVerified()) {
            throw new BusinessException("User account is not active");
        }

        return AuthenticatedUser.fromEntity(user);
    }
}

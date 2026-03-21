package com.blps.app.auth;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.repository.AppUserRepository;
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
        AppUser user = appUserRepository.findByLogin(login)
                .orElseGet(() -> appUserRepository.save(new AppUser(login)));
        return AuthenticatedUser.fromEntity(user);
    }
}

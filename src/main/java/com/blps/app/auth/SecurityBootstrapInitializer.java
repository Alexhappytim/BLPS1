package com.blps.app.auth;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;
import com.blps.app.domain.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
public class SecurityBootstrapInitializer {

    @Bean
    CommandLineRunner bootstrapDefaultUsers(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.bootstrap-admin.email:admin@blps.local}") String adminEmail,
            @Value("${app.auth.bootstrap-admin.password:admin12345}") String adminPassword
    ) {
        return args -> {
            ensureUser(appUserRepository, passwordEncoder, adminEmail, adminPassword, AppUserRole.ADMIN);
            ensureUser(appUserRepository, passwordEncoder, "mentor@blps.local", "mentor12345", AppUserRole.MENTOR);
            ensureUser(appUserRepository, passwordEncoder, "student@blps.local", "student12345", AppUserRole.USER);
        };
    }

    private void ensureUser(AppUserRepository repo, PasswordEncoder encoder, String email, String password, AppUserRole role) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (repo.existsByLogin(normalizedEmail)) {
            return;
        }
        AppUser user = new AppUser(
                normalizedEmail,
                encoder.encode(password),
                role,
                true,
                true
        );
        repo.save(user);
    }
}

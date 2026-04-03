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
    CommandLineRunner bootstrapAdmin(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.bootstrap-admin.email:admin@blps.local}") String adminEmail,
            @Value("${app.auth.bootstrap-admin.password:admin12345}") String adminPassword
    ) {
        return args -> {
            String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
            if (appUserRepository.existsByLogin(normalizedEmail)) {
                return;
            }
            AppUser admin = new AppUser(
                    normalizedEmail,
                    passwordEncoder.encode(adminPassword),
                    AppUserRole.ADMIN,
                    true,
                    true
            );
            appUserRepository.save(admin);
        };
    }
}

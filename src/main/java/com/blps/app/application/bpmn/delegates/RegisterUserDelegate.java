package com.blps.app.application.bpmn.delegates;

import com.blps.app.common.BusinessException;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.infrastructure.crm.CrmClient;
import com.blps.app.infrastructure.crm.dto.CrmUserUpsertRequest;
import com.blps.app.security.camunda.CamundaUserSyncService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Component("registerUserDelegate")
public class RegisterUserDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserDelegate.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdentityService identityService;
    private final CrmClient crmClient;

    public RegisterUserDelegate(AppUserRepository appUserRepository,
                                PasswordEncoder passwordEncoder,
                                IdentityService identityService,
                                CrmClient crmClient) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.identityService = identityService;
        this.crmClient = crmClient;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String email = (String) execution.getVariable("email");
        if (email == null || email.isBlank()) {
            email = (String) execution.getVariable("login");
        }
        String rawPassword = (String) execution.getVariable("password");

        if (email == null || email.isBlank()) {
            throw new BusinessException("Email/Логин обязателен для регистрации");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException("Пароль обязателен для регистрации");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        log.info("Processing registration in BPMN for user: {}", normalizedEmail);

        AppUser user;
        if (appUserRepository.existsByLogin(normalizedEmail)) {
            user = appUserRepository.findByLogin(normalizedEmail).orElseThrow();
            log.info("User {} already exists in DB, proceeding with existing user", normalizedEmail);
        } else {
            user = new AppUser(
                    normalizedEmail,
                    passwordEncoder.encode(rawPassword),
                    AppUserRole.USER,
                    true,
                    true
            );
            user = appUserRepository.save(user);

            try {
                crmClient.upsertUser(new CrmUserUpsertRequest(
                        user.getId(),
                        user.getLogin(),
                        user.getRole().name()
                ));
            } catch (Exception e) {
                log.warn("Failed to sync new user to CRM: {}", user.getLogin(), e);
            }
        }

        // Sync to Camunda identity service
        try {
            CamundaUserSyncService.syncUser(identityService, normalizedEmail, rawPassword, AppUserRole.USER);
        } catch (Exception e) {
            log.warn("Could not sync user to Camunda identity: {}", e.getMessage());
        }

        // Generate Basic Auth header
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                (normalizedEmail + ":" + rawPassword).getBytes(StandardCharsets.UTF_8)
        );

        // Save process variables for subsequent tasks
        execution.setVariable("login", normalizedEmail);
        execution.setVariable("email", normalizedEmail);
        execution.setVariable("password", rawPassword);
        execution.setVariable("authHeader", authHeader);
        execution.setVariable("userRole", "ROLE_USER");
        execution.setVariable("authenticated", true);

        log.info("Registration completed successfully in BPMN for: {}", normalizedEmail);
    }
}

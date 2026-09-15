package com.blps.app.application.bpmn.delegates;

import com.blps.app.common.BusinessException;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.repository.AppUserRepository;
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

@Component("loginUserDelegate")
public class LoginUserDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(LoginUserDelegate.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdentityService identityService;

    public LoginUserDelegate(AppUserRepository appUserRepository,
                             PasswordEncoder passwordEncoder,
                             IdentityService identityService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.identityService = identityService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String login = (String) execution.getVariable("login");
        String password = (String) execution.getVariable("password");

        if (login == null || login.isBlank()) {
            throw new BusinessException("Логин не может быть пустым");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException("Пароль не может быть пустым");
        }

        String normalizedLogin = login.trim().toLowerCase(Locale.ROOT);
        log.info("Processing login in BPMN for user: {}", normalizedLogin);

        AppUser user = appUserRepository.findByLogin(normalizedLogin)
                .orElseThrow(() -> new BusinessException("Пользователь с логином " + normalizedLogin + " не найден"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException("Неверный пароль для пользователя " + normalizedLogin);
        }

        // Sync to Camunda identity service
        try {
            CamundaUserSyncService.syncUser(identityService, normalizedLogin, password, user.getRole());
        } catch (Exception e) {
            log.warn("Could not sync user to Camunda identity: {}", e.getMessage());
        }

        // Generate Basic Auth header
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                (normalizedLogin + ":" + password).getBytes(StandardCharsets.UTF_8)
        );

        // Store variables for subsequent user tasks and REST operations
        execution.setVariable("login", normalizedLogin);
        execution.setVariable("password", password);
        execution.setVariable("authHeader", authHeader);
        execution.setVariable("userRole", "ROLE_" + user.getRole().name());
        execution.setVariable("authenticated", true);

        log.info("Login successful in BPMN for user: {}", normalizedLogin);
    }
}

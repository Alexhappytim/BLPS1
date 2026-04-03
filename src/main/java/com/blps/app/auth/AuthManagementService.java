package com.blps.app.auth;

import com.blps.app.auth.dto.AuthUserResponse;
import com.blps.app.auth.dto.RegistrationResponse;
import com.blps.app.common.BusinessException;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;
import com.blps.app.domain.model.EmailVerificationToken;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthManagementService {

    private final AppUserRepository appUserRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final boolean mailEnabled;
    private final String fromAddress;
    private final long tokenTtlHours;

    public AuthManagementService(AppUserRepository appUserRepository,
                                 EmailVerificationTokenRepository tokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 JavaMailSender javaMailSender,
                                 @Value("${app.mail.enabled:true}") boolean mailEnabled,
                                 @Value("${app.mail.from:${spring.mail.username:}}") String fromAddress,
                                 @Value("${app.auth.verification-token-ttl-hours:24}") long tokenTtlHours) {
        this.appUserRepository = appUserRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.javaMailSender = javaMailSender;
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
        this.tokenTtlHours = tokenTtlHours;
    }

    @Transactional
    public RegistrationResponse registerUser(String email, String rawPassword) {
        return createUser(email, rawPassword, AppUserRole.USER, false);
    }

    @Transactional
    public RegistrationResponse registerByAdmin(String email, String rawPassword, AppUserRole role) {
        if (role == AppUserRole.USER) {
            throw new BusinessException("Use public registration endpoint for USER role");
        }
        return createUser(email, rawPassword, role, true);
    }

    @Transactional
    public void confirmEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Verification token is invalid"));

        if (verificationToken.isConfirmed()) {
            throw new BusinessException("Verification token already used");
        }

        if (verificationToken.isExpired()) {
            throw new BusinessException("Verification token has expired");
        }

        AppUser user = verificationToken.getUser();
        user.enableAndMarkEmailVerified();
        verificationToken.markConfirmed();
        tokenRepository.deleteByUser(user);
    }

    @Transactional(readOnly = true)
    public AuthUserResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("User is not authenticated");
        }
        AppUser user = appUserRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new BusinessException("User is not found"));
        return new AuthUserResponse(user.getLogin(), user.getRole(), user.isEmailVerified(), user.isEnabled());
    }

    private RegistrationResponse createUser(String email, String rawPassword, AppUserRole role, boolean activateImmediately) {
        String normalizedEmail = normalizeEmail(email);
        if (appUserRepository.existsByLogin(normalizedEmail)) {
            throw new BusinessException("User with this email already exists");
        }

        AppUser user = new AppUser(
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                role,
                activateImmediately,
                activateImmediately
        );
        appUserRepository.save(user);

        if (activateImmediately) {
            return new RegistrationResponse(
                    normalizedEmail,
                    role,
                    false,
                    null,
                    "User created and activated"
            );
        }

        String token = UUID.randomUUID().toString();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(tokenTtlHours);
        tokenRepository.save(new EmailVerificationToken(token, user, expiresAt));
        sendVerificationEmail(user.getLogin(), token);

        String exposedToken = mailEnabled ? null : token;
        String message = mailEnabled
                ? "Verification email sent"
                : "Mail is disabled. Use verificationToken from response for manual confirmation";

        return new RegistrationResponse(
                normalizedEmail,
                role,
                true,
                exposedToken,
                message
        );
    }

    private void sendVerificationEmail(String recipient, String token) {
        if (!mailEnabled) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject("Confirm your BLPS account");
        message.setText("Use this token to confirm your email: " + token);
        javaMailSender.send(message);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

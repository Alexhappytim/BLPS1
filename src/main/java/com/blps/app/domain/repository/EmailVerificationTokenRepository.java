package com.blps.app.domain.repository;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    void deleteByUser(AppUser user);
}

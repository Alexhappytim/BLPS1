package com.blps.app.domain.repository;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByLogin(String login);

    boolean existsByLogin(String login);

    List<AppUser> findByRole(AppUserRole role);

    List<AppUser> findByEnabledTrueAndEmailVerifiedTrueAndLastLoginAtGreaterThanEqualAndLastLoginAtLessThan(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    );
}

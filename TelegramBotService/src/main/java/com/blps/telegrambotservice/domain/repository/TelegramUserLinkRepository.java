package com.blps.telegrambotservice.domain.repository;

import com.blps.telegrambotservice.domain.TelegramUserLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramUserLinkRepository extends JpaRepository<TelegramUserLink, Long> {

    Optional<TelegramUserLink> findByEmail(String email);
}

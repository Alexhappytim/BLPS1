package com.blps.app.infrastructure.scheduling;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.infrastructure.messaging.mail.EmailCommandType;
import com.blps.app.infrastructure.messaging.mail.MailDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
public class InactivityReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(InactivityReminderScheduler.class);

    private final AppUserRepository appUserRepository;
    private final MailDispatchService mailDispatchService;
    private final ZoneId zoneId;

    public InactivityReminderScheduler(AppUserRepository appUserRepository,
                                      MailDispatchService mailDispatchService) {
        this.appUserRepository = appUserRepository;
        this.mailDispatchService = mailDispatchService;
        this.zoneId = ZoneId.systemDefault();
    }

    @Scheduled(cron = "${app.scheduling.inactivity-reminder-cron:0 * * * * *}")
    public void sendMonthlyInactivityReminders() {
        if (!mailDispatchService.isMailEnabled()) {
            return;
        }

        var users = appUserRepository.findByEnabledTrueAndEmailVerifiedTrueAndInactivityReminderCountLessThan(3);
        if (users.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now(zoneId);
        int dispatched = 0;

        for (AppUser user : users) {
            String to = user.getLogin();
            if (to == null || !to.contains("@")) {
                continue;
            }

            OffsetDateTime lastActivity = user.getLastLoginAt() != null ? user.getLastLoginAt() : OffsetDateTime.MIN;
            if (lastActivity.plusDays(3).isAfter(now)) {
                continue; // User was active less than 3 days ago
            }

            OffsetDateTime lastReminder = user.getLastInactivityReminderAt();
            if (lastReminder != null && lastReminder.plusDays(3).isAfter(now)) {
                continue; // We already sent a reminder less than 3 days ago
            }

            int count = user.getInactivityReminderCount();
            String subject = "We miss you on BLPS";
            String body = "";
            String imageUrl = null;

            if (count == 0) {
                body = "Hello! We noticed you haven't logged in for a few days. Come back and continue learning: https://blps.local";
                imageUrl = "sad_cat_step1.png";
            } else if (count == 1) {
                body = "It's been a while... We are really sad without you. Please come back: https://blps.local";
                imageUrl = "sad_cat_step2.png";
            } else if (count == 2) {
                subject = "Final reminder...";
                body = "We are crying in the rain. This is our last reminder. We hope to see you again soon: https://blps.local";
                imageUrl = "sad_cat_step3.png";
            }

            boolean ok = mailDispatchService.dispatch(
                    EmailCommandType.INACTIVITY_REMINDER,
                    to,
                    subject,
                    body,
                    imageUrl
            );
            
            if (ok) {
                user.incrementInactivityReminderCount();
                appUserRepository.save(user);
                dispatched++;
            }
        }

        if (dispatched > 0) {
            log.info("Dispatched {} progressive inactivity reminders", dispatched);
        }
    }
}

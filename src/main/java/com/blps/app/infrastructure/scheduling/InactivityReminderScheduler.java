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

    @Scheduled(cron = "${app.scheduling.inactivity-reminder-cron:0 0 3 * * *}")
    public void sendMonthlyInactivityReminders() {
        if (!mailDispatchService.isMailEnabled()) {
            return;
        }

        LocalDate targetDate = LocalDate.now(zoneId).minusMonths(1);
        OffsetDateTime start = targetDate.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime end = targetDate.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        var users = appUserRepository.findByEnabledTrueAndEmailVerifiedTrueAndLastLoginAtGreaterThanEqualAndLastLoginAtLessThan(start, end);
        if (users.isEmpty()) {
            return;
        }

        int dispatched = 0;
        for (AppUser user : users) {
            String to = user.getLogin();
            if (to == null || !to.contains("@")) {
                continue;
            }

            boolean ok = mailDispatchService.dispatch(
                    EmailCommandType.INACTIVITY_REMINDER,
                    to,
                    "We miss you on BLPS",
                    "Hello! We noticed you haven't logged in for a month. Come back and continue learning: https://blps.local"
            );
            if (ok) {
                dispatched++;
            }
        }

        log.info("Dispatched inactivity reminders: {} (targetDate={})", dispatched, targetDate);
    }
}

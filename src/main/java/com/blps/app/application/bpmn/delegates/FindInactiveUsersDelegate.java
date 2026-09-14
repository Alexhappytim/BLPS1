package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.repository.AppUserRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component("findInactiveUsersDelegate")
public class FindInactiveUsersDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(FindInactiveUsersDelegate.class);

    private final AppUserRepository appUserRepository;
    private final ZoneId zoneId;

    public FindInactiveUsersDelegate(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
        this.zoneId = ZoneId.systemDefault();
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Executing FindInactiveUsersDelegate for Inactivity Reminder process");

        List<AppUser> users = appUserRepository.findByEnabledTrueAndEmailVerifiedTrueAndInactivityReminderCountLessThan(3);
        List<String> inactiveUsers = new ArrayList<>();

        if (!users.isEmpty()) {
            OffsetDateTime now = OffsetDateTime.now(zoneId);

            for (AppUser user : users) {
                String login = user.getLogin();
                if (login == null || !login.contains("@")) {
                    continue;
                }

                OffsetDateTime lastActivity = user.getLastLoginAt() != null ? user.getLastLoginAt() : OffsetDateTime.MIN;
                if (lastActivity.plusDays(3).isAfter(now)) {
                    continue; // User was active less than 3 days ago
                }

                OffsetDateTime lastReminder = user.getLastInactivityReminderAt();
                if (lastReminder != null && lastReminder.plusDays(3).isAfter(now)) {
                    continue; // Already sent a reminder less than 3 days ago
                }

                inactiveUsers.add(login);
            }
        }

        log.info("Found {} inactive users eligible for reminder", inactiveUsers.size());
        execution.setVariable("inactiveUsers", inactiveUsers);
    }
}

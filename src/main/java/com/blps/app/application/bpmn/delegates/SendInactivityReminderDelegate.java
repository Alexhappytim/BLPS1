package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.infrastructure.messaging.mail.EmailCommandType;
import com.blps.app.infrastructure.messaging.mail.MailDispatchService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("sendInactivityReminderDelegate")
public class SendInactivityReminderDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(SendInactivityReminderDelegate.class);

    private final AppUserRepository appUserRepository;
    private final MailDispatchService mailDispatchService;

    public SendInactivityReminderDelegate(AppUserRepository appUserRepository,
                                          MailDispatchService mailDispatchService) {
        this.appUserRepository = appUserRepository;
        this.mailDispatchService = mailDispatchService;
    }

    @Override
    @Transactional
    public void execute(DelegateExecution execution) throws Exception {
        Object userObj = execution.getVariable("userLogin");
        if (userObj == null) {
            userObj = execution.getVariable("user");
        }
        if (userObj == null) {
            log.warn("No user login provided in multi-instance execution {}", execution.getId());
            return;
        }

        String to = userObj.toString();
        log.info("Sending inactivity reminder to: {}", to);

        AppUser user = appUserRepository.findByLogin(to).orElse(null);
        if (user == null) {
            log.warn("User {} not found in database", to);
            return;
        }

        int count = user.getInactivityReminderCount();
        String subject = "We miss you on BLPS";
        String body;
        String imageUrl = null;

        if (count == 0) {
            body = "Hello! We noticed you haven't logged in for a few days. Come back and continue learning: https://blps.local";
            imageUrl = "sad_cat_step1.png";
        } else if (count == 1) {
            body = "It's been a while... We are really sad without you. Please come back: https://blps.local";
            imageUrl = "sad_cat_step2.png";
        } else {
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
            log.info("Successfully dispatched progressive inactivity reminder (step {}) to {}", count + 1, to);
        } else {
            log.warn("Failed to dispatch inactivity reminder to {}", to);
        }
    }
}

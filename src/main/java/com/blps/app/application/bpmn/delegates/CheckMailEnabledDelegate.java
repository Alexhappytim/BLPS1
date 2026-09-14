package com.blps.app.application.bpmn.delegates;

import com.blps.app.infrastructure.messaging.mail.MailDispatchService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("checkMailEnabledDelegate")
public class CheckMailEnabledDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(CheckMailEnabledDelegate.class);

    private final MailDispatchService mailDispatchService;

    public CheckMailEnabledDelegate(MailDispatchService mailDispatchService) {
        this.mailDispatchService = mailDispatchService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        boolean mailEnabled = mailDispatchService.isMailEnabled();
        log.info("Checking mail enabled for Inactivity Reminder process: {}", mailEnabled);
        execution.setVariable("mailEnabled", mailEnabled);
    }
}

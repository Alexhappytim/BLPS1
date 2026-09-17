package com.blps.app.application.bpmn.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("initLoginListener")
public class InitLoginListener implements ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(InitLoginListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Object loginVar = execution.getVariable("login");
        if (loginVar == null || loginVar.toString().isBlank()) {
            Object initiator = execution.getVariable("initiator");
            if (initiator != null && !initiator.toString().isBlank()) {
                execution.setVariable("login", initiator.toString());
                log.info("Initialized process variable 'login' from initiator: {}", initiator);
            } else {
                String bk = execution.getProcessBusinessKey();
                if (bk != null && bk.contains(":")) {
                    String loginFromBk = bk.substring(0, bk.indexOf(":"));
                    execution.setVariable("login", loginFromBk);
                    log.info("Initialized process variable 'login' from businessKey: {}", loginFromBk);
                } else {
                    String authenticatedUser = execution.getProcessEngineServices()
                            .getIdentityService()
                            .getCurrentAuthentication() != null
                            ? execution.getProcessEngineServices().getIdentityService().getCurrentAuthentication().getUserId()
                            : null;
                    if (authenticatedUser != null && !authenticatedUser.isBlank()) {
                        execution.setVariable("login", authenticatedUser);
                        log.info("Initialized process variable 'login' from current authentication: {}", authenticatedUser);
                    } else {
                        execution.setVariable("login", "student@blps.local");
                        log.info("Defaulted process variable 'login' to student@blps.local");
                    }
                }
            }
        } else {
            log.info("Process already has login: {}", loginVar);
        }
    }
}

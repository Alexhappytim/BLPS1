package com.blps.app.application.bpmn.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("autoReviewDelegate")
public class AutoReviewDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(AutoReviewDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String login = (String) execution.getVariable("login");
        Object taskIdVar = execution.getVariable("taskId");
        Object solutionVar = execution.getVariable("solution");

        log.info("Executing auto review for user: {}, task: {}", login, taskIdVar);

        // Automated validation: solution is valid and auto-approved
        boolean approved = solutionVar != null && !solutionVar.toString().trim().isEmpty();
        execution.setVariable("approved", approved);
        execution.setVariable("requiresReview", false);
        log.info("Auto review completed with result: approved={}", approved);
    }
}

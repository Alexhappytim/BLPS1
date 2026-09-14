package com.blps.app.application.bpmn.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("resolveCoefficientDelegate")
public class ResolveCoefficientDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(ResolveCoefficientDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String activityId = execution.getCurrentActivityId();
        double coeff = 1.0;

        if ("Activity_0ajhbu1".equals(activityId)) {
            coeff = 1.0;
        } else if ("Activity_1qs93wg".equals(activityId)) {
            coeff = 1.5;
        } else if ("Activity_0ujyb5p".equals(activityId)) {
            coeff = 2.0;
        } else {
            Object diffVar = execution.getVariable("difficulty");
            String difficulty = diffVar != null ? diffVar.toString().toLowerCase() : "easy";
            coeff = switch (difficulty) {
                case "medium" -> 1.5;
                case "hard" -> 2.0;
                default -> 1.0;
            };
        }

        log.info("Setting difficulty coefficient: {} for execution: {}", coeff, execution.getId());
        execution.setVariable("coefficient", coeff);
        execution.setVariable("coeff", coeff);
    }
}

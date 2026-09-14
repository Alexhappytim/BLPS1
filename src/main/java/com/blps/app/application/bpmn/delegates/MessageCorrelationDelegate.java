package com.blps.app.application.bpmn.delegates;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("messageCorrelationDelegate")
public class MessageCorrelationDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(MessageCorrelationDelegate.class);

    private final RuntimeService runtimeService;

    public MessageCorrelationDelegate(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String messageName = resolveMessageName(execution);
        String businessKey = execution.getBusinessKey();
        if (businessKey == null || businessKey.isBlank()) {
            businessKey = execution.getProcessBusinessKey();
        }
        if (businessKey == null || businessKey.isBlank()) {
            Object loginVar = execution.getVariable("login");
            Object courseIdVar = execution.getVariable("courseId");
            businessKey = (loginVar != null ? loginVar.toString() : "user") + ":"
                    + (courseIdVar != null ? courseIdVar.toString() : "1");
            execution.setProcessBusinessKey(businessKey);
        }

        Map<String, Object> variables = execution.getVariables();
        log.info("Correlating BPMN message [{}] with businessKey [{}] from element [{}]",
                messageName, businessKey, execution.getCurrentActivityId());

        try {
            runtimeService.createMessageCorrelation(messageName)
                    .processInstanceBusinessKey(businessKey)
                    .setVariables(variables)
                    .correlateAll();
        } catch (Exception e) {
            log.warn("CorrelateAll failed for message [{}], attempting correlateStartMessage or fallback: {}",
                    messageName, e.getMessage());
            try {
                runtimeService.correlateMessage(messageName, businessKey, variables);
            } catch (Exception ex) {
                log.error("Could not correlate message [{}]: {}", messageName, ex.getMessage());
            }
        }
    }

    private String resolveMessageName(DelegateExecution execution) {
        if (execution.getBpmnModelElementInstance() instanceof ThrowEvent throwEvent) {
            for (EventDefinition def : throwEvent.getEventDefinitions()) {
                if (def instanceof MessageEventDefinition med && med.getMessage() != null) {
                    return med.getMessage().getName();
                }
            }
        }

        String activityId = execution.getCurrentActivityId();
        return switch (activityId) {
            case "Event_1oj3xmw" -> "Message_DiffChoice";
            case "Event_00b1p5e" -> "Message_CoeffReady";
            case "Event_1fobr8n" -> "Message_TaskSubmit";
            case "Event_11xayda", "Activity_1phiz1k" -> "Message_ReviewReq";
            case "Event_1tnbgv8" -> "Message_ReviewResult";
            case "Event_10q87k1" -> "Message_PointsCredited";
            case "Event_040koyh" -> "Message_CheckPoints";
            case "Activity_174cjbu" -> "Message_NotEnoughPoints";
            case "Event_02rnazz" -> "Message_BlockOpened";
            case "Event_0punqpd" -> "Message_FinishCourse";
            case "Event_15t2n9g" -> "Message_CertificateSent";
            default -> activityId;
        };
    }
}

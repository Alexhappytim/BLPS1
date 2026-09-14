package com.blps.app.application.service;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CamundaProcessService {

    private static final Logger log = LoggerFactory.getLogger(CamundaProcessService.class);

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public CamundaProcessService(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    public ProcessInstance startLearningProcess(String login, Long courseId, String difficulty) {
        String businessKey = login + ":" + courseId;
        Map<String, Object> variables = new HashMap<>();
        variables.put("login", login);
        variables.put("courseId", courseId);
        variables.put("difficulty", difficulty != null ? difficulty.toLowerCase() : "easy");

        log.info("Starting Process_00qvb5l for user {} with businessKey {}", login, businessKey);
        return runtimeService.startProcessInstanceByKey("Process_00qvb5l", businessKey, variables);
    }

    public ProcessInstance triggerInactivityReminderProcess() {
        log.info("Manually starting Inactivity Reminder process (Process_0sz4ezi)");
        return runtimeService.startProcessInstanceByKey("Process_0sz4ezi");
    }

    public void triggerTaskSubmit(String login, Long courseId, Long taskId, String difficulty, String solution, boolean requiresReview, String reviewType) {
        String businessKey = login + ":" + courseId;
        Map<String, Object> variables = new HashMap<>();
        variables.put("login", login);
        variables.put("courseId", courseId);
        variables.put("taskId", taskId);
        variables.put("difficulty", difficulty != null ? difficulty.toLowerCase() : "easy");
        variables.put("solution", solution != null ? solution : "solution");
        variables.put("requiresReview", requiresReview);
        variables.put("reviewType", reviewType != null ? reviewType.toLowerCase() : "auto");
        variables.put("isRetry", false);

        log.info("Triggering Message_TaskSubmit for businessKey: {}", businessKey);
        try {
            runtimeService.createMessageCorrelation("Message_TaskSubmit")
                    .processInstanceBusinessKey(businessKey)
                    .setVariables(variables)
                    .correlateAll();
        } catch (Exception e) {
            log.warn("Message_TaskSubmit direct correlation failed: {}, trying to correlate message", e.getMessage());
            try {
                runtimeService.correlateMessage("Message_TaskSubmit", businessKey, variables);
            } catch (Exception ex) {
                log.info("Process instance not waiting for Message_TaskSubmit: {}", ex.getMessage());
            }
        }
    }

    public void triggerMentorReviewResult(String login, Long courseId, Long taskId, boolean approved, String feedback, Long mentorId) {
        String businessKey = login + ":" + courseId;
        Map<String, Object> variables = new HashMap<>();
        variables.put("login", login);
        variables.put("courseId", courseId);
        variables.put("taskId", taskId);
        variables.put("approved", approved);
        variables.put("feedback", feedback);
        variables.put("mentorId", mentorId);

        log.info("Triggering Message_ReviewResult for businessKey: {}", businessKey);
        try {
            runtimeService.createMessageCorrelation("Message_ReviewResult")
                    .processInstanceBusinessKey(businessKey)
                    .setVariables(variables)
                    .correlateAll();
        } catch (Exception e) {
            log.warn("Message_ReviewResult correlation failed: {}", e.getMessage());
        }
    }

    public void triggerOpenBlock(String login, Long courseId, Long blockId) {
        String businessKey = login + ":" + courseId;
        Map<String, Object> variables = new HashMap<>();
        variables.put("login", login);
        variables.put("courseId", courseId);
        variables.put("blockId", blockId);

        log.info("Triggering Message_CheckPoints for businessKey: {}", businessKey);
        try {
            runtimeService.createMessageCorrelation("Message_CheckPoints")
                    .processInstanceBusinessKey(businessKey)
                    .setVariables(variables)
                    .correlateAll();
        } catch (Exception e) {
            log.warn("Message_CheckPoints correlation info: {}", e.getMessage());
        }
    }

    public List<Task> getActiveTasksForGroup(String candidateGroup) {
        return taskService.createTaskQuery().taskCandidateGroup(candidateGroup).list();
    }

    public List<Task> getActiveTasksForUser(String login) {
        return taskService.createTaskQuery().taskAssignee(login).list();
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        log.info("Completing Camunda task {} with variables {}", taskId, variables);
        taskService.complete(taskId, variables);
    }
}

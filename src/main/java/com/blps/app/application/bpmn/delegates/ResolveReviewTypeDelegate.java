package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.LearningTask;
import com.blps.app.domain.repository.LearningTaskRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("resolveReviewTypeDelegate")
public class ResolveReviewTypeDelegate implements JavaDelegate, ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ResolveReviewTypeDelegate.class);

    private final LearningTaskRepository learningTaskRepository;

    public ResolveReviewTypeDelegate(LearningTaskRepository learningTaskRepository) {
        this.learningTaskRepository = learningTaskRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        resolve(execution);
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        resolve(execution);
    }

    private void resolve(DelegateExecution execution) {
        Object taskIdVar = execution.getVariable("taskId");
        Long taskId = null;
        if (taskIdVar instanceof Number num) {
            taskId = num.longValue();
        } else if (taskIdVar instanceof String str && !str.isBlank()) {
            try {
                taskId = Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        if (taskId != null) {
            LearningTask task = learningTaskRepository.findById(taskId).orElse(null);
            if (task != null && task.getReviewType() != null) {
                String reviewType = task.getReviewType().name().toLowerCase();
                execution.setVariable("reviewType", reviewType);
                log.info("Auto-resolved reviewType='{}' from database for taskId={}", reviewType, taskId);
                return;
            }
        }

        // Fallback: if not found in DB, default to "auto"
        Object existing = execution.getVariable("reviewType");
        if (existing == null) {
            execution.setVariable("reviewType", "auto");
            log.info("Defaulting reviewType to 'auto' for taskId={}", taskId);
        }
    }
}

package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.CourseBlock;
import com.blps.app.domain.model.LearningTask;
import com.blps.app.domain.repository.CourseBlockRepository;
import com.blps.app.domain.repository.CourseRepository;
import com.blps.app.domain.repository.LearningTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.plugin.variable.SpinValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("loadFormOptionsListener")
public class LoadFormOptionsListener implements TaskListener, ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(LoadFormOptionsListener.class);

    private final CourseRepository courseRepository;
    private final CourseBlockRepository courseBlockRepository;
    private final LearningTaskRepository learningTaskRepository;
    private final ObjectMapper objectMapper;

    public LoadFormOptionsListener(CourseRepository courseRepository,
                                  CourseBlockRepository courseBlockRepository,
                                  LearningTaskRepository learningTaskRepository,
                                  ObjectMapper objectMapper) {
        this.courseRepository = courseRepository;
        this.courseBlockRepository = courseBlockRepository;
        this.learningTaskRepository = learningTaskRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        populateOptions(delegateTask.getExecution(), delegateTask.getTaskDefinitionKey());
    }

    @Override
    public void notify(DelegateExecution execution) {
        populateOptions(execution, execution.getCurrentActivityId());
    }

    private void populateOptions(DelegateExecution execution, String activityId) {
        if (activityId == null) {
            return;
        }

        try {
            Object loginVar = execution.getVariable("login");
            if (loginVar == null || loginVar.toString().isBlank()) {
                Object initiator = execution.getVariable("initiator");
                if (initiator != null && !initiator.toString().isBlank()) {
                    execution.setVariable("login", initiator.toString());
                } else {
                    String bk = execution.getProcessBusinessKey();
                    if (bk != null && bk.contains(":")) {
                        execution.setVariable("login", bk.substring(0, bk.indexOf(":")));
                    }
                }
            }

            switch (activityId) {
                case "Activity_1kedmqx" -> loadCourseOptions(execution);
                case "Activity_1exmn8d" -> loadBlockOptions(execution);
                case "Activity_1q5ps4n", "Activity_1v7bsv8" -> loadTaskOptions(execution);
                default -> {
                    // Preload all if unspecified
                    loadCourseOptions(execution);
                    loadBlockOptions(execution);
                    loadTaskOptions(execution);
                }
            }
        } catch (Exception e) {
            log.warn("Error loading dynamic form options for activity {}: {}", activityId, e.getMessage(), e);
        }
    }

    private void loadCourseOptions(DelegateExecution execution) {
        List<Course> courses = courseRepository.findAll();
        List<Map<String, String>> options = new ArrayList<>();
        for (Course c : courses) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("label", c.getTitle() + " — " + c.getPrice() + " ₽ (ID: " + c.getId() + ")");
            item.put("value", String.valueOf(c.getId()));
            options.add(item);
        }
        setJsonVariable(execution, "availableCourses", options);
    }

    private void loadBlockOptions(DelegateExecution execution) {
        Long courseId = getLongVariable(execution, "courseId");
        List<CourseBlock> blocks;
        if (courseId != null) {
            blocks = courseBlockRepository.findByCourseIdWithCourse(courseId);
        } else {
            blocks = courseBlockRepository.findAll();
        }

        List<Map<String, String>> options = new ArrayList<>();
        for (CourseBlock b : blocks) {
            Map<String, String> item = new LinkedHashMap<>();
            String courseTitle = b.getCourse() != null ? b.getCourse().getTitle() : "Курс";
            item.put("label", b.getTitle() + " (" + courseTitle + ", " + b.getOpenCost() + " б.) — ID: " + b.getId());
            item.put("value", String.valueOf(b.getId()));
            options.add(item);
        }
        setJsonVariable(execution, "availableBlocks", options);
    }

    private void loadTaskOptions(DelegateExecution execution) {
        Long blockId = getLongVariable(execution, "blockId");
        Long courseId = getLongVariable(execution, "courseId");

        // If blockId is not set yet, attempt to resolve first block of current course
        if (blockId == null && courseId != null) {
            List<CourseBlock> blocks = courseBlockRepository.findByCourseIdWithCourse(courseId);
            if (!blocks.isEmpty()) {
                blockId = blocks.get(0).getId();
                execution.setVariable("blockId", blockId);
            }
        }

        List<LearningTask> tasks;
        if (blockId != null) {
            tasks = learningTaskRepository.findByBlockIdWithBlockAndCourse(blockId);
        } else if (courseId != null) {
            tasks = learningTaskRepository.findByCourseIdWithBlock(courseId);
        } else {
            tasks = learningTaskRepository.findAll();
        }

        List<Map<String, String>> options = new ArrayList<>();
        for (LearningTask t : tasks) {
            Map<String, String> item = new LinkedHashMap<>();
            String reviewLabel = t.getReviewType() != null ? t.getReviewType().name() : "AUTO";
            item.put("label", t.getId() + " — " + t.getTitle() + " (" + reviewLabel + ", " + t.getBasePoints() + " б.)");
            item.put("value", String.valueOf(t.getId()));
            options.add(item);
        }
        setJsonVariable(execution, "availableTasks", options);
    }

    private void setJsonVariable(DelegateExecution execution, String varName, Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            execution.setVariable(varName, SpinValues.jsonValue(json).create());
            log.info("Populated dynamic form option variable [{}] as JSON (items: {})",
                    varName, (object instanceof List<?> list ? list.size() : 1));
        } catch (Exception e) {
            log.warn("Failed to serialize variable [{}] to Camunda JSON: {}. Falling back to default object.",
                    varName, e.getMessage());
            execution.setVariable(varName, object);
        }
    }

    private Long getLongVariable(DelegateExecution execution, String name) {
        Object val = execution.getVariable(name);
        if (val instanceof Number num) {
            return num.longValue();
        }
        if (val instanceof String str && !str.isBlank()) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}

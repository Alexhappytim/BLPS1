package com.blps.app.web.controller;

import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.application.service.SubmissionResult;
import com.blps.app.auth.AuthenticationService;
import com.blps.app.domain.model.LearningTask;
import com.blps.app.web.dto.PagedResponse;
import com.blps.app.web.dto.SubmissionResponse;
import com.blps.app.web.dto.SubmitTaskRequest;
import com.blps.app.web.dto.TaskDto;
import com.blps.app.web.dto.TaskUpsertRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@Validated
public class TasksController {

    private final AuthenticationService authenticationService;
    private final LearningPlatformService learningPlatformService;

    public TasksController(AuthenticationService authenticationService, LearningPlatformService learningPlatformService) {
        this.authenticationService = authenticationService;
        this.learningPlatformService = learningPlatformService;
    }

    @GetMapping
    public PagedResponse<TaskDto> tasks(
            @RequestParam @Positive Long courseId,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        var resultPage = learningPlatformService.tasks(courseId, page);
        var data = resultPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return PagedResponse.from(resultPage, data);
    }

    @GetMapping("/{id}")
    public TaskDto taskById(@PathVariable @Positive Long id) {
        return toDto(learningPlatformService.taskById(id));
    }

    @PostMapping
    public TaskDto createTask(@Valid @RequestBody TaskUpsertRequest request) {
        LearningTask created = learningPlatformService.createTask(
                request.blockId(),
                request.code(),
                request.title(),
                request.basePoints(),
                request.reviewType()
        );
        return toDto(created);
    }

    @PutMapping("/{id}")
    public TaskDto updateTask(@PathVariable @Positive Long id, @Valid @RequestBody TaskUpsertRequest request) {
        LearningTask updated = learningPlatformService.updateTask(
                id,
                request.blockId(),
                request.code(),
                request.title(),
                request.basePoints(),
                request.reviewType()
        );
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable @Positive Long id) {
        learningPlatformService.deleteTask(id);
    }

    @PostMapping("/submit")
    public SubmissionResponse submitTask(@Valid @RequestBody SubmitTaskRequest request) {
        authenticationService.authenticateByLogin(request.login());
        SubmissionResult result = learningPlatformService.submitTask(
                request.login(),
                request.courseId(),
                request.taskId(),
            request.difficulty()
        );
        return new SubmissionResponse(
                result.submissionId(),
                result.status(),
                result.calculatedPoints(),
                result.awardedPoints(),
                result.userPoints()
        );
    }

        private TaskDto toDto(LearningTask task) {
        return new TaskDto(
            task.getId(),
            task.getBlock().getCourse().getId(),
            task.getCode(),
            task.getTitle(),
            task.getBlock().getId(),
            task.getBasePoints(),
            task.getReviewType()
        );
        }
}

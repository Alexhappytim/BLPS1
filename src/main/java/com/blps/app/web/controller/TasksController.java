package com.blps.app.web.controller;

import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.application.service.SubmissionResult;
import com.blps.app.auth.AuthenticationService;
import com.blps.app.web.dto.SubmissionResponse;
import com.blps.app.web.dto.SubmitTaskRequest;
import com.blps.app.web.dto.TaskDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List<TaskDto> tasks(@RequestParam @Positive Long courseId) {
        return learningPlatformService.tasks(courseId).stream()
                .map(task -> new TaskDto(
                        task.getId(),
                        task.getBlock().getCourse().getId(),
                        task.getCode(),
                        task.getTitle(),
                        task.getBlock().getId(),
                        task.getBasePoints(),
                        task.isRequiresMentorReview()
                ))
                .toList();
    }

    @PostMapping("/submit")
    public SubmissionResponse submitTask(@Valid @RequestBody SubmitTaskRequest request) {
        authenticationService.authenticateByLogin(request.login());
        SubmissionResult result = learningPlatformService.submitTask(
                request.login(),
                request.courseId(),
                request.taskId(),
                request.difficulty(),
                request.reviewType()
        );
        return new SubmissionResponse(
                result.submissionId(),
                result.status(),
                result.calculatedPoints(),
                result.awardedPoints(),
                result.userPoints()
        );
    }
}

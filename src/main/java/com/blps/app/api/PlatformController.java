package com.blps.app.api;

import com.blps.app.api.dto.BlockDto;
import com.blps.app.api.dto.CoefficientResponse;
import com.blps.app.api.dto.LoginCarrier;
import com.blps.app.api.dto.OpenBlockRequest;
import com.blps.app.api.dto.OpenBlockResponse;
import com.blps.app.api.dto.ResolveCoefficientRequest;
import com.blps.app.api.dto.ReviewSubmissionRequest;
import com.blps.app.api.dto.SubmissionResponse;
import com.blps.app.api.dto.SubmitTaskRequest;
import com.blps.app.api.dto.TaskDto;
import com.blps.app.api.dto.UserPointsRequest;
import com.blps.app.api.dto.UserPointsResponse;
import com.blps.app.auth.AuthenticationService;
import com.blps.app.domain.service.BlockOpenResult;
import com.blps.app.domain.service.LearningPlatformService;
import com.blps.app.domain.service.SubmissionResult;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class PlatformController {

    private final AuthenticationService authenticationService;
    private final LearningPlatformService learningPlatformService;

    public PlatformController(AuthenticationService authenticationService, LearningPlatformService learningPlatformService) {
        this.authenticationService = authenticationService;
        this.learningPlatformService = learningPlatformService;
    }

    @PostMapping("/blocks/resolve-coefficient")
    public CoefficientResponse resolveCoefficient(@Valid @RequestBody ResolveCoefficientRequest request) {
        authenticate(request);
        double coefficient = learningPlatformService.resolveCoefficient(request.difficulty());
        return new CoefficientResponse(request.difficulty(), coefficient);
    }

    @PostMapping("/tasks/submit")
    public SubmissionResponse submitTask(@Valid @RequestBody SubmitTaskRequest request) {
        authenticate(request);
        SubmissionResult result = learningPlatformService.submitTask(
                request.login(),
                request.taskId(),
                request.difficulty(),
                request.reviewType()
        );
        return toResponse(result);
    }

    @PostMapping("/submissions/review")
    public SubmissionResponse reviewSubmission(@Valid @RequestBody ReviewSubmissionRequest request) {
        authenticate(request);
        SubmissionResult result = learningPlatformService.reviewSubmission(
                request.login(),
                request.submissionId(),
                request.approved()
        );
        return toResponse(result);
    }

    @PostMapping("/blocks/open")
    public OpenBlockResponse openBlock(@Valid @RequestBody OpenBlockRequest request) {
        authenticate(request);
        BlockOpenResult result = learningPlatformService.openBlock(request.login(), request.blockId());
        return new OpenBlockResponse(result.blockId(), result.alreadyOpened(), result.remainingPoints());
    }

    @PostMapping("/users/points")
    public UserPointsResponse userPoints(@Valid @RequestBody UserPointsRequest request) {
        authenticate(request);
        long points = learningPlatformService.userPoints(request.login());
        return new UserPointsResponse(request.login(), points);
    }

    @GetMapping("/blocks")
    public List<BlockDto> blocks() {
        return learningPlatformService.blocks().stream()
                .map(block -> new BlockDto(
                        block.getId(),
                        block.getCode(),
                        block.getTitle(),
                        block.getOpenCost(),
                        block.getOrderIndex()
                ))
                .toList();
    }

    @GetMapping("/tasks")
    public List<TaskDto> tasks() {
        return learningPlatformService.tasks().stream()
                .map(task -> new TaskDto(
                        task.getId(),
                        task.getCode(),
                        task.getTitle(),
                        task.getBlock().getId(),
                        task.getBasePoints(),
                        task.isRequiresMentorReview()
                ))
                .toList();
    }

    private void authenticate(LoginCarrier request) {
        authenticationService.authenticateByLogin(request.login());
    }

    private SubmissionResponse toResponse(SubmissionResult result) {
        return new SubmissionResponse(
                result.submissionId(),
                result.status(),
                result.calculatedPoints(),
                result.awardedPoints(),
                result.userPoints()
        );
    }
}

package com.blps.app.web.controller;

import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.application.service.SubmissionResult;
import com.blps.app.auth.AuthenticationService;
import com.blps.app.web.dto.ReviewSubmissionRequest;
import com.blps.app.web.dto.SubmissionResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionsController {

    private final AuthenticationService authenticationService;
    private final LearningPlatformService learningPlatformService;

    public SubmissionsController(AuthenticationService authenticationService, LearningPlatformService learningPlatformService) {
        this.authenticationService = authenticationService;
        this.learningPlatformService = learningPlatformService;
    }

    @PostMapping("/review")
    public SubmissionResponse reviewSubmission(@Valid @RequestBody ReviewSubmissionRequest request) {
        authenticationService.authenticateByLogin(request.login());
        SubmissionResult result = learningPlatformService.reviewSubmission(
                request.login(),
                request.courseId(),
                request.submissionId(),
                request.approved()
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

package com.blps.app.web.controller;

import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.auth.AuthenticationService;
import com.blps.app.web.dto.UserPointsRequest;
import com.blps.app.web.dto.UserPointsResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final AuthenticationService authenticationService;
    private final LearningPlatformService learningPlatformService;

    public UsersController(AuthenticationService authenticationService, LearningPlatformService learningPlatformService) {
        this.authenticationService = authenticationService;
        this.learningPlatformService = learningPlatformService;
    }

    @PostMapping("/points")
    public UserPointsResponse userPoints(@Valid @RequestBody UserPointsRequest request) {
        authenticationService.authenticateByLogin(request.login());
        long points = learningPlatformService.userPoints(request.login(), request.courseId());
        return new UserPointsResponse(request.login(), request.courseId(), points);
    }
}

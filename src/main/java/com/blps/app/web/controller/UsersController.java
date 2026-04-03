package com.blps.app.web.controller;

import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.auth.AuthenticationService;
import com.blps.app.web.dto.UserPointsRequest;
import com.blps.app.web.dto.UserPointsResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if (!hasPrivilegedPointsAccess(authentication) && !authentication.getName().equalsIgnoreCase(request.login())) {
            throw new AccessDeniedException("Only admin or mentor can query another user's points");
        }

        if (!hasPrivilegedPointsAccess(authentication)) {
            authenticationService.authenticateByLogin(request.login());
        }

        long points = learningPlatformService.userPoints(request.login(), request.courseId());
        return new UserPointsResponse(request.login(), request.courseId(), points);
    }

    private boolean hasPrivilegedPointsAccess(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()) || "ROLE_MENTOR".equals(authority.getAuthority()));
    }
}

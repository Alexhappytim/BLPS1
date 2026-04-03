package com.blps.app.auth;

import com.blps.app.auth.dto.AdminRegisterRequest;
import com.blps.app.auth.dto.AuthUserResponse;
import com.blps.app.auth.dto.ConfirmEmailRequest;
import com.blps.app.auth.dto.RegisterRequest;
import com.blps.app.auth.dto.RegistrationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthManagementService authManagementService;

    public AuthController(AuthManagementService authManagementService) {
        this.authManagementService = authManagementService;
    }

    @PostMapping("/register")
    public RegistrationResponse register(@Valid @RequestBody RegisterRequest request) {
        return authManagementService.registerUser(request.email(), request.password());
    }

    @PostMapping("/admin/register")
    public RegistrationResponse registerByAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        return authManagementService.registerByAdmin(request.email(), request.password(), request.role());
    }

    @PostMapping("/confirm-email")
    public void confirmEmail(@Valid @RequestBody ConfirmEmailRequest request) {
        authManagementService.confirmEmail(request.token());
    }

    @GetMapping("/login")
    public AuthUserResponse login() {
        return authManagementService.me();
    }

    @GetMapping("/me")
    public AuthUserResponse me() {
        return authManagementService.me();
    }
}

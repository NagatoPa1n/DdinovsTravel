package uz.nagato.touragency.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.auth.dto.AuthResponse;
import uz.nagato.touragency.auth.dto.LoginRequest;
import uz.nagato.touragency.auth.dto.RefreshTokenRequest;
import uz.nagato.touragency.auth.dto.RegisterRequest;
import uz.nagato.touragency.auth.dto.UserResponse;
import uz.nagato.touragency.auth.service.AuthService;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.user.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Registration successful", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.message("Logged out");
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> logoutEverywhere() {
        authService.logoutEverywhere(userService.currentUser());
        return ApiResponse.message("Logged out from all devices");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(UserResponse.from(userService.currentUser()));
    }
}

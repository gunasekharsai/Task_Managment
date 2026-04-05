package com.taskmanagment.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanagment.app.Dto.AuthRequest;
import com.taskmanagment.app.Dto.AuthResponseDto;
import com.taskmanagment.app.Dto.Responses;
import com.taskmanagment.app.Dto.UserSummaryDto;
import com.taskmanagment.app.security.UserPrincipal;
import com.taskmanagment.app.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
// @Tag(name = "Authentication", description = "Register, login, token refresh, and password management")
public class AuthController {
 
    private final AuthService authService;
 
    @PostMapping("/register")
    // @Operation(summary = "Register a new user account")
    public ResponseEntity<Responses<AuthResponseDto>> register(
            @Valid @RequestBody AuthRequest.Register request) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Responses.ok("Account created successfully", response));
    }
 
    @PostMapping("/login")
    // @Operation(summary = "Log in with email/username and password")
    public ResponseEntity<Responses<AuthResponseDto>> login(
            @Valid @RequestBody AuthRequest.Login request) {
        return ResponseEntity.ok(Responses.ok(authService.login(request)));
    }
 
    @PostMapping("/refresh")
    // @Operation(summary = "Exchange a refresh token for a new access token")
    public ResponseEntity<Responses<AuthResponseDto>> refresh(
            @Valid @RequestBody AuthRequest.RefreshToken request) {
        return ResponseEntity.ok(Responses.ok(authService.refreshToken(request.getRefreshToken())));
    }
 
    @PostMapping("/logout")
    // @Operation(summary = "Logout (client should discard tokens)")
    public ResponseEntity<Responses<Void>> logout() {
        // JWT is stateless — logout is handled client-side.
        // For production, add a token blacklist / Redis denylist here.
        return ResponseEntity.ok(Responses.ok("Logged out successfully", null));
    }
 
    @GetMapping("/me")
    // @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<Responses<UserSummaryDto>> getMe(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Responses.ok(authService.getProfile(principal.getId())));
    }
 
    @PutMapping("/change-password")
    // @Operation(summary = "Change the authenticated user's password")
    public ResponseEntity<Responses<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AuthRequest.ChangePassword request) {
        authService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(Responses.ok("Password changed successfully", null));
    }
}

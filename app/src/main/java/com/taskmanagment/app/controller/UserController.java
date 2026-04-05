package com.taskmanagment.app.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanagment.app.Dto.Responses;
import com.taskmanagment.app.Dto.UserSummaryDto;
import com.taskmanagment.app.Dto.UserUpdateRequest;
import com.taskmanagment.app.security.UserPrincipal;
import com.taskmanagment.app.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
 
    @GetMapping("/me")
    // @Operation(summary = "Get current user's full profile")
    public ResponseEntity<Responses<UserSummaryDto>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Responses.ok(userService.getProfile(principal.getId())));
    }
 
    @GetMapping("/{userId}")
    // @Operation(summary = "Get another user's public profile")
    public ResponseEntity<Responses<UserSummaryDto>> getUserProfile(
            @PathVariable String userId) {
        return ResponseEntity.ok(Responses.ok(userService.getProfile(userId)));
    }
 
    @PutMapping("/me")
    // @Operation(summary = "Update current user's profile")
    public ResponseEntity<Responses<UserSummaryDto>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(Responses.ok(
            "Profile updated", userService.updateProfile(principal.getId(), request)));
    }
}

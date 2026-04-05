package com.taskmanagment.app.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthRequest {
 
    @Data
    public static class Register {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        private String username;
 
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
 
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
        private String password;
 
        @Size(max = 100)
        private String fullName;
    }
 
    @Data
    public static class Login {
        @NotBlank(message = "Email or username is required")
        private String emailOrUsername;
 
        @NotBlank(message = "Password is required")
        private String password;
    }
 
    @Data
    public static class RefreshToken {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
 
    @Data
    public static class ChangePassword {
        @NotBlank(message = "Current password is required")
        private String currentPassword;
 
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100)
        private String newPassword;
    }
}

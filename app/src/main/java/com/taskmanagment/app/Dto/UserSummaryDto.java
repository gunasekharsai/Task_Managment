package com.taskmanagment.app.Dto;

import java.time.LocalDateTime;

import com.taskmanagment.app.Models.UserModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserSummaryDto {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private UserModel.Role role;
    private LocalDateTime createdAt;
 
    public static UserSummaryDto from(UserModel u) {
        return UserSummaryDto.builder()
            .id(u.getId()).username(u.getUsername()).email(u.getEmail())
            .fullName(u.getFullName()).bio(u.getBio()).avatarUrl(u.getAvatarUrl())
            .role(u.getRole()).createdAt(u.getCreatedAt()).build();
    }
}



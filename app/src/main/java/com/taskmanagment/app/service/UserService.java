package com.taskmanagment.app.service;

import org.springframework.web.multipart.MultipartFile;

import com.taskmanagment.app.Dto.UserSummaryDto;
import com.taskmanagment.app.Dto.UserUpdateRequest;

public interface UserService {
    UserSummaryDto getProfile(String userId);
    UserSummaryDto updateProfile(String userId, UserUpdateRequest request);
    UserSummaryDto uploadAvatar(String userId, MultipartFile file);
}
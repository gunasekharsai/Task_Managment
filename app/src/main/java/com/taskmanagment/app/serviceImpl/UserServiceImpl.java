package com.taskmanagment.app.serviceImpl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.taskmanagment.app.Dto.UserSummaryDto;
import com.taskmanagment.app.Dto.UserUpdateRequest;
import com.taskmanagment.app.Exceptions.BadRequestException;
import com.taskmanagment.app.Exceptions.ResourceNotFoundException;
import com.taskmanagment.app.Models.UserModel;
import com.taskmanagment.app.Repository.UserRepository;
import com.taskmanagment.app.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements  UserService {

    @Autowired
    public UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getProfile(String userId) {
        // TODO Auto-generated method stub
        return UserSummaryDto.from(findUser(userId));
    }

    @Override
    public UserSummaryDto updateProfile(String userId, UserUpdateRequest request) {
        // TODO Auto-generated method stub
        UserModel user = findUser(userId);
 
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already taken: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getBio()      != null) user.setBio(request.getBio());
 
        return UserSummaryDto.from(userRepository.save(user));
       }

    @Override
    public UserSummaryDto uploadAvatar(String userId, MultipartFile file) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadAvatar'");
    }


    private UserModel findUser(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
    
}

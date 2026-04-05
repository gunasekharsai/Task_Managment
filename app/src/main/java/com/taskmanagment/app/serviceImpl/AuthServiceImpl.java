package com.taskmanagment.app.serviceImpl;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskmanagment.app.Dto.AuthRequest;
import com.taskmanagment.app.Dto.AuthResponseDto;
import com.taskmanagment.app.Dto.UserSummaryDto;
import com.taskmanagment.app.Exceptions.BadRequestException;
import com.taskmanagment.app.Exceptions.ResourceNotFoundException;
import com.taskmanagment.app.Models.UserModel;
import com.taskmanagment.app.Repository.UserRepository;
import com.taskmanagment.app.security.JwtTokenProvider;
import com.taskmanagment.app.security.UserPrincipal;
import com.taskmanagment.app.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;


    @Override
    @Transactional
    public AuthResponseDto register(AuthRequest.Register request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email is already in use: "+request.getEmail());
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new BadRequestException("Username is already taken: "+request.getUsername());
        }
        
        UserModel user = UserModel.builder().
        username(request.getUsername()).
        email(request.getEmail().toLowerCase()).
        password(passwordEncoder.encode(request.getPassword())).
        fullName(request.getFullName()).
        role(UserModel.Role.USER).
        enabled(true).
        createdAt(LocalDateTime.now()).
        build();

        userRepository.save(user);
        log.info("New user registered: "+user.getEmail());
        
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        return buildAuthResponse(auth, user);
    }

    @Override
    @Transactional
    public AuthResponseDto login(AuthRequest.Login request) {
        Authentication auth  = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmailOrUsername(), request.getPassword()));

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        UserModel user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new BadRequestException("User not found"));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(auth, user);
    }

    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }
        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
 
        String newAccessToken = tokenProvider.generateAccessTokenFromUserId(userId);
        String newRefreshToken = tokenProvider.generateRefreshToken(userId);
 
        return AuthResponseDto.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .user(UserSummaryDto.from(user))
            .build();
    }

    @Override
    @Transactional
    public void changePassword(String userId, AuthRequest.ChangePassword request) {
        UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
        throw new BadRequestException("Current password is incorrect");
    }
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    log.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getProfile(String userId) {
        UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    return UserSummaryDto.from(user);
    }
 
    private AuthResponseDto buildAuthResponse(Authentication auth, UserModel user) {
        String accessToken = tokenProvider.generateAccessToken(auth);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());
        return AuthResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .user(UserSummaryDto.from(user))
            .build();
    }


    
}

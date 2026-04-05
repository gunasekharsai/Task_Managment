package com.taskmanagment.app.service;

import com.taskmanagment.app.Dto.AuthRequest;
import com.taskmanagment.app.Dto.AuthResponseDto;
import com.taskmanagment.app.Dto.UserSummaryDto;

public interface  AuthService {
    public AuthResponseDto register(AuthRequest.Register request);
    public AuthResponseDto login(AuthRequest.Login request) ;
    public AuthResponseDto refreshToken(String refreshToken);

    public void changePassword(String userId, AuthRequest.ChangePassword request);
    public UserSummaryDto getProfile(String userId) ;

}

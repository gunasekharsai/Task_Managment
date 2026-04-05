package com.taskmanagment.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.taskmanagment.app.Dto.TeamRequest;
import com.taskmanagment.app.Dto.TeamResponseDto;

public interface TeamService {
    TeamResponseDto createTeam(String ownerId, TeamRequest.Create request);
    TeamResponseDto getTeam(String teamId, String requesterId);
    TeamResponseDto updateTeam(String teamId, String requesterId, TeamRequest.Update request);
    void deleteTeam(String teamId, String requesterId);
 
    Page<TeamResponseDto> getMyTeams(String userId, Pageable pageable);
 
    void inviteMember(String teamId, String requesterId, String inviteeEmail);
    TeamResponseDto acceptInvite(String token, String userId);
    void removeMember(String teamId, String requesterId, String memberId);
    void leaveTeam(String teamId, String userId);
}

package com.taskmanagment.app.serviceImpl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskmanagment.app.Dto.TeamRequest;
import com.taskmanagment.app.Dto.TeamResponseDto;
import com.taskmanagment.app.Exceptions.AccessDeniedException;
import com.taskmanagment.app.Exceptions.BadRequestException;
import com.taskmanagment.app.Exceptions.ResourceNotFoundException;
import com.taskmanagment.app.Models.TeamInvite;
import com.taskmanagment.app.Models.TeamModel;
import com.taskmanagment.app.Models.UserModel;
import com.taskmanagment.app.Repository.TeamInviteRepository;
import com.taskmanagment.app.Repository.TeamRepository;
import com.taskmanagment.app.Repository.UserRepository;
import com.taskmanagment.app.service.TeamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamInviteRepository inviteRepository;
    // private final NotificationService notificationService;
 
    @Override
    @Transactional
    public TeamResponseDto createTeam(String ownerId, TeamRequest.Create request) {
        UserModel owner = findUser(ownerId);
 
        TeamModel team = TeamModel.builder()
            .name(request.getName())
            .description(request.getDescription())
            .owner(owner)
            .build();
        team.getMembers().add(owner); // owner is also a member
 
        TeamModel saved = teamRepository.save(team);
        log.info("Team created [{}] by user [{}]", saved.getId(), ownerId);
        return TeamResponseDto.from(saved);
    }
 
    @Override
    @Transactional(readOnly = true)
    public TeamResponseDto getTeam(String teamId, String requesterId) {
        TeamModel team = findTeam(teamId);
        assertMember(team, requesterId);
        return TeamResponseDto.from(team);
    }
 
    @Override
    @Transactional
    public TeamResponseDto updateTeam(String teamId, String requesterId, TeamRequest.Update request) {
        TeamModel team = findTeam(teamId);
        assertOwner(team, requesterId);
 
        if (request.getName()        != null) team.setName(request.getName());
        if (request.getDescription() != null) team.setDescription(request.getDescription());
 
        return TeamResponseDto.from(teamRepository.save(team));
    }
 
    @Override
    @Transactional
    public void deleteTeam(String teamId, String requesterId) {
        TeamModel team = findTeam(teamId);
        assertOwner(team, requesterId);
        teamRepository.delete(team);
        log.info("Team deleted [{}] by user [{}]", teamId, requesterId);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponseDto> getMyTeams(String userId, Pageable pageable) {
        return teamRepository.findByMemberId(userId, pageable).map(TeamResponseDto::from);
    }
 
    @Override
    @Transactional
    public void inviteMember(String teamId, String requesterId, String inviteeEmail) {
        TeamModel team = findTeam(teamId);
        assertOwner(team, requesterId);
 
        // Check if already a member
        boolean alreadyMember = team.getMembers().stream()
            .anyMatch(m -> m.getEmail().equalsIgnoreCase(inviteeEmail));
        if (alreadyMember) {
            throw new BadRequestException("User is already a member of this team");
        }
 
        // Check for pending invite
        if (inviteRepository.existsByTeamIdAndInviteeEmailAndStatus(
                teamId, inviteeEmail, TeamInvite.Status.PENDING)) {
            throw new BadRequestException("A pending invite already exists for this email");
        }
 
        UserModel inviter = findUser(requesterId);
        String token = UUID.randomUUID().toString();
 
        TeamInvite invite = TeamInvite.builder()
            .token(token)
            .inviteeEmail(inviteeEmail.toLowerCase())
            .team(team)
            .invitedBy(inviter)
            .status(TeamInvite.Status.PENDING)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build();
 
        inviteRepository.save(invite);
        log.info("Invite sent to [{}] for team [{}]", inviteeEmail, teamId);
 
        // Notify if user already registered
        // userRepository.findByEmail(inviteeEmail).ifPresent(user ->
        //     notificationService.createAndSend(
        //         user.getId(),
        //         Notification.Type.TEAM_INVITE,
        //         "You have been invited to join team: " + team.getName(),
        //         teamId, "TEAM"));
    }
 
    @Override
    @Transactional
    public TeamResponseDto acceptInvite(String token, String userId) {
        TeamInvite invite = inviteRepository.findByToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("Invite not found for token: " + token));
 
        if (invite.getStatus() != TeamInvite.Status.PENDING) {
            throw new BadRequestException("Invite is no longer valid");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(TeamInvite.Status.EXPIRED);
            inviteRepository.save(invite);
            throw new BadRequestException("Invite has expired");
        }
 
        UserModel user = findUser(userId);
        if (!user.getEmail().equalsIgnoreCase(invite.getInviteeEmail())) {
            throw new AccessDeniedException("This invite was sent to a different email address");
        }
 
        TeamModel team = invite.getTeam();
        team.getMembers().add(user);
        invite.setStatus(TeamInvite.Status.ACCEPTED);
 
        teamRepository.save(team);
        inviteRepository.save(invite);
 
        log.info("User [{}] joined team [{}]", userId, team.getId());
 
        // notificationService.createAndSend(
        //     team.getOwner().getId(),
        //     Notification.Type.TEAM_JOINED,
        //     user.getUsername() + " has joined your team: " + team.getName(),
        //     team.getId(), "TEAM");
 
        return TeamResponseDto.from(team);
    }
 
    @Override
    @Transactional
    public void removeMember(String teamId, String requesterId, String memberId) {
        TeamModel team = findTeam(teamId);
        assertOwner(team, requesterId);
 
        if (memberId.equals(team.getOwner().getId())) {
            throw new BadRequestException("Cannot remove the team owner");
        }
 
        UserModel member = findUser(memberId);
        boolean removed = team.getMembers().remove(member);
        if (!removed) {
            throw new BadRequestException("User is not a member of this team");
        }
        teamRepository.save(team);
        log.info("Member [{}] removed from team [{}] by [{}]", memberId, teamId, requesterId);
    }
 
    @Override
    @Transactional
    public void leaveTeam(String teamId, String userId) {
        TeamModel team = findTeam(teamId);
 
        if (userId.equals(team.getOwner().getId())) {
            throw new BadRequestException("Team owner cannot leave. Transfer ownership or delete the team.");
        }
 
        UserModel user = findUser(userId);
        boolean removed = team.getMembers().remove(user);
        if (!removed) {
            throw new BadRequestException("You are not a member of this team");
        }
        teamRepository.save(team);
        log.info("User [{}] left team [{}]", userId, teamId);
    }
 
    // ── Helpers ───────────────────────────────────────────────────────────────
 
    private TeamModel findTeam(String teamId) {
        return teamRepository.findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
    }
 
    private UserModel findUser(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
 
    private void assertOwner(TeamModel team, String userId) {
        if (!team.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the team owner can perform this action");
        }
    }
 
    private void assertMember(TeamModel team, String userId) {
        boolean isMember = team.getMembers().stream().anyMatch(m -> m.getId().equals(userId))
            || team.getOwner().getId().equals(userId);
        if (!isMember) {
            throw new AccessDeniedException("You are not a member of this team");
        }
    }
    
}

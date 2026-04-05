package com.taskmanagment.app.controller;


// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanagment.app.Dto.Responses;
import com.taskmanagment.app.Dto.TeamRequest;
import com.taskmanagment.app.Dto.TeamResponseDto;
import com.taskmanagment.app.security.UserPrincipal;
import com.taskmanagment.app.service.TeamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
// @Tag(name = "Teams", description = "Team/project management and membership")
public class TeamController {
 
    private final TeamService teamService;
 
    @PostMapping
    // @Operation(summary = "Create a new team")
    public ResponseEntity<Responses<TeamResponseDto>> createTeam(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TeamRequest.Create request) {
        TeamResponseDto team = teamService.createTeam(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Responses.ok("Team created", team));
    }
 
    @GetMapping("/{teamId}")
    // @Operation(summary = "Get team details")
    public ResponseEntity<Responses<TeamResponseDto>> getTeam(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String teamId) {
        return ResponseEntity.ok(Responses.ok(teamService.getTeam(teamId, principal.getId())));
    }
 
    @PutMapping("/{teamId}")
    // @Operation(summary = "Update team details (owner only)")
    public ResponseEntity<Responses<TeamResponseDto>> updateTeam(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String teamId,
            @Valid @RequestBody TeamRequest.Update request) {
        return ResponseEntity.ok(Responses.ok(
            "Team updated", teamService.updateTeam(teamId, principal.getId(), request)));
    }
 
    @DeleteMapping("/{teamId}")
    // @Operation(summary = "Delete a team (owner only)")
    public ResponseEntity<Responses<Void>> deleteTeam(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String teamId) {
        teamService.deleteTeam(teamId, principal.getId());
        return ResponseEntity.ok(Responses.ok("Team deleted", null));
    }
 
    @GetMapping("/my")
    // @Operation(summary = "Get all teams the current user belongs to")
    public ResponseEntity<Responses<Page<TeamResponseDto>>> getMyTeams(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(Responses.ok(teamService.getMyTeams(principal.getId(), pageable)));
    }
 
    // ── Member Management ─────────────────────────────────────────────────────
 
    @PostMapping("/{teamId}/invite")
    // @Operation(summary = "Invite a user to the team by email (owner only)")
    public ResponseEntity<Responses<Void>> inviteMember(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String teamId,
            @Valid @RequestBody TeamRequest.Invite request) {
        teamService.inviteMember(teamId, principal.getId(), request.getEmail());
        return ResponseEntity.ok(Responses.ok("Invitation sent to " + request.getEmail(), null));
    }
 
    @PostMapping("/invites/{token}/accept")
    // @Operation(summary = "Accept a team invitation using the token from the invite email")
    public ResponseEntity<Responses<TeamResponseDto>> acceptInvite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String token) {
        return ResponseEntity.ok(Responses.ok(
            "Successfully joined the team", teamService.acceptInvite(token, principal.getId())));
    }
 
    @DeleteMapping("/{teamId}/members/{memberId}")
    // @Operation(summary = "Remove a member from the team (owner only)")
    public ResponseEntity<Responses<Void>> removeMember(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String teamId,
            @PathVariable String memberId) {
        teamService.removeMember(teamId, principal.getId(), memberId);
        return ResponseEntity.ok(Responses.ok("Member removed", null));
    }
 
    @PostMapping("/{teamId}/leave")
    // @Operation(summary = "Leave a team")
    public ResponseEntity<Responses<Void>> leaveTeam(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String teamId) {
        teamService.leaveTeam(teamId, principal.getId());
        return ResponseEntity.ok(Responses.ok("You have left the team", null));
    }
}
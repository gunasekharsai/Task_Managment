package com.taskmanagment.app.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagment.app.Models.TeamInvite;

@Repository
public interface TeamInviteRepository extends JpaRepository<TeamInvite, String> {
 
    Optional<TeamInvite> findByToken(String token);
 
    boolean existsByTeamIdAndInviteeEmailAndStatus(String teamId, String email, TeamInvite.Status status);
}
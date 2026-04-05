package com.taskmanagment.app.Dto;

import java.time.LocalDateTime;
import java.util.List;

import com.taskmanagment.app.Models.TeamModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TeamResponseDto {
    private String id;
    private String name;
    private String description;
    private UserSummaryDto owner;
    private List<UserSummaryDto> members;
    private int memberCount;
    private LocalDateTime createdAt;
 
    public static TeamResponseDto from(TeamModel t) {
        List<UserSummaryDto> members = t.getMembers().stream().map(UserSummaryDto::from).toList();
        return TeamResponseDto.builder()
            .id(t.getId()).name(t.getName()).description(t.getDescription())
            .owner(UserSummaryDto.from(t.getOwner()))
            .members(members).memberCount(members.size())
            .createdAt(t.getCreatedAt()).build();
    }
}
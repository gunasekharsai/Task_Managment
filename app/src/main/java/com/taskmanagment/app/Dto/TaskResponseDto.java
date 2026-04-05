package com.taskmanagment.app.Dto;


import java.time.LocalDate;
import java.time.LocalDateTime;

import com.taskmanagment.app.Models.TaskModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaskResponseDto {
    private String id;
    private String title;
    private String description;
    private TaskModel.Status status;
    private TaskModel.Priority priority;
    private LocalDate dueDate;
    private UserSummaryDto creator;
    private UserSummaryDto assignee;
    private String teamId;
    private String teamName;
    private long commentCount;
    private long attachmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
 
    public static TaskResponseDto from(TaskModel t) {
        return TaskResponseDto.builder()
            .id(t.getId()).title(t.getTitle()).description(t.getDescription())
            .status(t.getStatus()).priority(t.getPriority()).dueDate(t.getDueDate())
            .creator(t.getCreator() != null ? UserSummaryDto.from(t.getCreator()) : null)
            .assignee(t.getAssignee() != null ? UserSummaryDto.from(t.getAssignee()) : null)
            .teamId(t.getTeam() != null ? t.getTeam().getId() : null)
            .teamName(t.getTeam() != null ? t.getTeam().getName() : null)
            .commentCount(t.getComments() != null ? t.getComments().size() : 0)
            .attachmentCount(t.getAttachments() != null ? t.getAttachments().size() : 0)
            .createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt()).completedAt(t.getCompletedAt())
            .build();
    }
}

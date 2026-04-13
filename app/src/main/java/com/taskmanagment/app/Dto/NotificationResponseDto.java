package com.taskmanagment.app.Dto;

import java.time.LocalDateTime;

import com.taskmanagment.app.Models.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResponseDto {
    private String id;
    private Notification.Type type;
    private String message;
    private String referenceId;
    private String referenceType;
    private boolean read;
    private LocalDateTime createdAt;
 
    public static NotificationResponseDto from(Notification n) {
        return NotificationResponseDto.builder()
            .id(n.getId()).type(n.getType()).message(n.getMessage())
            .referenceId(n.getReferenceId()).referenceType(n.getReferenceType())
            .read(n.isRead()).createdAt(n.getCreatedAt()).build();
    }
}
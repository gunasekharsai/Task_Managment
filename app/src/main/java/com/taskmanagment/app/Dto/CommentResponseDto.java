package com.taskmanagment.app.Dto;

import java.time.LocalDateTime;

import com.taskmanagment.app.Models.Comments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommentResponseDto {
    private String id;
    private String content;
    private UserSummaryDto author;
    private String taskId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
 
    public static CommentResponseDto from(Comments c) {
        return CommentResponseDto.builder()
            .id(c.getId()).content(c.getContent())
            .author(UserSummaryDto.from(c.getAuthor()))
            .taskId(c.getTask().getId())
            .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
    }
}

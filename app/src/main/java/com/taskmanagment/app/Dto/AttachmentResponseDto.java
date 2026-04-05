package com.taskmanagment.app.Dto;

import java.time.LocalDateTime;

import com.taskmanagment.app.Models.Attachment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AttachmentResponseDto {
    private String id;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private UserSummaryDto uploadedBy;
    private LocalDateTime uploadedAt;
 
    public static AttachmentResponseDto from(Attachment a) {
        return AttachmentResponseDto.builder()
            .id(a.getId()).originalFileName(a.getOriginalFileName())
            .fileType(a.getFileType()).fileSize(a.getFileSize())
            .downloadUrl("/api/v1/attachments/download/" + a.getId())
            .uploadedBy(UserSummaryDto.from(a.getUploadedBy()))
            .uploadedAt(a.getUploadedAt()).build();
    }
}
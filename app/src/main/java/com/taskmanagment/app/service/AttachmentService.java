package com.taskmanagment.app.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.taskmanagment.app.Dto.AttachmentResponseDto;



public interface AttachmentService {
    AttachmentResponseDto uploadAttachment(String taskId, String uploaderId, MultipartFile file);
    List<AttachmentResponseDto> getAttachments(String taskId);
    Resource downloadAttachment(String attachmentId, String requesterId);
    void deleteAttachment(String attachmentId, String requesterId);
}
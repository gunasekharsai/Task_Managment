package com.taskmanagment.app.serviceImpl;

import java.util.List;
import java.util.Objects;

import org.springframework.core.io.Resource;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.taskmanagment.app.Dto.AttachmentResponseDto;
import com.taskmanagment.app.Exceptions.AccessDeniedException;
import com.taskmanagment.app.Exceptions.ResourceNotFoundException;
import com.taskmanagment.app.Models.Attachment;
import com.taskmanagment.app.Models.TaskModel;
import com.taskmanagment.app.Models.UserModel;
import com.taskmanagment.app.Repository.AttachmentRepository;
import com.taskmanagment.app.Repository.TaskRepository;
import com.taskmanagment.app.Repository.UserRepository;
import com.taskmanagment.app.service.AttachmentService;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final FileStorageUtil fileStorageUtil;
 
    @Override
    @Transactional
    public AttachmentResponseDto uploadAttachment(String taskId, String uploaderId, MultipartFile file) {
        TaskModel task = findTask(taskId);
        UserModel uploader = findUser(uploaderId);
 
        String relativePath = fileStorageUtil.storeFile(file, "attachments");
 
        Attachment attachment = Attachment.builder()
            .originalFileName(Objects.requireNonNull(file.getOriginalFilename()))
            .storedFileName(relativePath.substring(relativePath.lastIndexOf('/') + 1))
            .fileType(file.getContentType())
            .fileSize(file.getSize())
            .filePath(relativePath)
            .task(task)
            .uploadedBy(uploader)
            .build();
 
        return AttachmentResponseDto.from(attachmentRepository.save(attachment));
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponseDto> getAttachments(String taskId) {
        return attachmentRepository.findByTaskId(taskId)
            .stream().map(AttachmentResponseDto::from).toList();
    }
 
    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(String attachmentId, String requesterId) {
        Attachment attachment = findAttachment(attachmentId);
        assertCanAccess(attachment.getTask(), requesterId);
        return fileStorageUtil.loadFileAsResource(attachment.getFilePath());
    }
 
    @Override
    @Transactional
    public void deleteAttachment(String attachmentId, String requesterId) {
        Attachment attachment = findAttachment(attachmentId);
 
        boolean isUploader    = attachment.getUploadedBy().getId().equals(requesterId);
        boolean isTaskCreator = attachment.getTask().getCreator().getId().equals(requesterId);
        if (!isUploader && !isTaskCreator) {
            throw new AccessDeniedException("You cannot delete this attachment");
        }
 
        fileStorageUtil.deleteFile(attachment.getFilePath());
        attachmentRepository.delete(attachment);
    }
 
    // ── Helpers ───────────────────────────────────────────────────────────────
 
    private TaskModel findTask(String id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
    }
 
    private UserModel findUser(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
 
    private Attachment findAttachment(String id) {
        return attachmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));
    }
 
    private void assertCanAccess(TaskModel task, String userId) {
        boolean isCreator    = task.getCreator().getId().equals(userId);
        boolean isAssignee   = task.getAssignee() != null && task.getAssignee().getId().equals(userId);
        boolean isTeamMember = task.getTeam() != null
            && task.getTeam().getMembers().stream().anyMatch(m -> m.getId().equals(userId));
        if (!isCreator && !isAssignee && !isTeamMember) {
            throw new AccessDeniedException("You do not have access to this attachment");
        }
    }
}

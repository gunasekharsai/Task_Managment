package com.taskmanagment.app.serviceImpl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskmanagment.app.Dto.CommentRequest;
import com.taskmanagment.app.Dto.CommentResponseDto;
import com.taskmanagment.app.Exceptions.AccessDeniedException;
import com.taskmanagment.app.Exceptions.ResourceNotFoundException;
import com.taskmanagment.app.Models.Comments;
import com.taskmanagment.app.Models.Notification;
import com.taskmanagment.app.Models.TaskModel;
import com.taskmanagment.app.Models.UserModel;
import com.taskmanagment.app.Repository.CommentRepository;
import com.taskmanagment.app.Repository.TaskRepository;
import com.taskmanagment.app.Repository.UserRepository;
import com.taskmanagment.app.service.CommentService;
import com.taskmanagment.app.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
 
    @Override
    @Transactional
    public CommentResponseDto addComment(String taskId, String authorId, CommentRequest request) {
        TaskModel task = findTask(taskId);
        UserModel author = findUser(authorId);
 
        Comments comment = Comments.builder()
            .content(request.getContent())
            .task(task)
            .author(author)
            .createdAt(LocalDateTime.now())
            .build();
 
        Comments saved = commentRepository.save(comment);
 
        // Notify task assignee (if different from commenter)
        if (task.getAssignee() != null && !task.getAssignee().getId().equals(authorId)) {
            notificationService.createAndSend(
                task.getAssignee().getId(),
                Notification.Type.COMMENT_ADDED,
                author.getUsername() + " commented on task: " + task.getTitle(),
                task.getId(), "TASK");
        }
        // Notify task creator (if different from commenter and assignee)
        if (!task.getCreator().getId().equals(authorId)
                && (task.getAssignee() == null || !task.getCreator().getId().equals(task.getAssignee().getId()))) {
            notificationService.createAndSend(
                task.getCreator().getId(),
                Notification.Type.COMMENT_ADDED,
                author.getUsername() + " commented on task: " + task.getTitle(),
                task.getId(), "TASK");
        }
 
        return CommentResponseDto.from(saved);
    }
 
    @Override
    @Transactional
    public CommentResponseDto updateComment(String commentId, String requesterId, CommentRequest request) {
        Comments comment = findComment(commentId);
        assertAuthor(comment, requesterId);
        comment.setContent(request.getContent());
        return CommentResponseDto.from(commentRepository.save(comment));
    }
 
    @Override
    @Transactional
    public void deleteComment(String commentId, String requesterId) {
        Comments comment = findComment(commentId);
        // Author or task creator can delete
        boolean isAuthor      = comment.getAuthor().getId().equals(requesterId);
        boolean isTaskCreator = comment.getTask().getCreator().getId().equals(requesterId);
        if (!isAuthor && !isTaskCreator) {
            throw new AccessDeniedException("You cannot delete this comment");
        }
        commentRepository.delete(comment);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getComments(String taskId, Pageable pageable) {
        return commentRepository.findByTaskId(taskId, pageable).map(CommentResponseDto::from);
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
 
    private Comments findComment(String id) {
        return commentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }
 
    private void assertAuthor(Comments comment, String userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Only the comment author can perform this action");
        }
    }
    
}

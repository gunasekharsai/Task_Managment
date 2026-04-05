package com.taskmanagment.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.taskmanagment.app.Dto.CommentRequest;
import com.taskmanagment.app.Dto.CommentResponseDto;

public interface CommentService {
    CommentResponseDto addComment(String taskId, String authorId, CommentRequest request);
    CommentResponseDto updateComment(String commentId, String requesterId, CommentRequest request);
    void deleteComment(String commentId, String requesterId);
    Page<CommentResponseDto> getComments(String taskId, Pageable pageable);
}

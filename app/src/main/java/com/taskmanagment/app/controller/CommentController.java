package com.taskmanagment.app.controller;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanagment.app.Dto.CommentRequest;
import com.taskmanagment.app.Dto.CommentResponseDto;
import com.taskmanagment.app.Dto.Responses;
import com.taskmanagment.app.security.UserPrincipal;
import com.taskmanagment.app.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
// @Tag(name = "Comments", description = "Task comments — add, edit, delete, list")
public class CommentController {

    private final CommentService commentService;

    // ── Add comment ───────────────────────────────────────────────────────────

    @PostMapping("/tasks/{taskId}/comments")
    // @Operation(summary = "Add a comment to a task")
    public ResponseEntity<Responses<CommentResponseDto>> addComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String taskId,
            @Valid @RequestBody CommentRequest request) {

        CommentResponseDto comment = commentService.addComment(taskId, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Responses.ok("Comment added", comment));
    }

    // ── Get comments ──────────────────────────────────────────────────────────

    @GetMapping("/tasks/{taskId}/comments")
    // @Operation(summary = "List all comments on a task (paginated, oldest first)")
    public ResponseEntity<Responses<Page<CommentResponseDto>>> getComments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String taskId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return ResponseEntity.ok(Responses.ok(commentService.getComments(taskId, pageable)));
    }

    // ── Update comment ────────────────────────────────────────────────────────

    @PutMapping("/comments/{commentId}")
    // @Operation(summary = "Edit a comment (author only)")
    public ResponseEntity<Responses<CommentResponseDto>> updateComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String commentId,
            @Valid @RequestBody CommentRequest request) {

        return ResponseEntity.ok(Responses.ok(
                "Comment updated",
                commentService.updateComment(commentId, principal.getId(), request)));
    }

    // ── Delete comment ────────────────────────────────────────────────────────

    @DeleteMapping("/comments/{commentId}")
    // @Operation(summary = "Delete a comment (author or task creator)")
    public ResponseEntity<Responses<Void>> deleteComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String commentId) {

        commentService.deleteComment(commentId, principal.getId());
        return ResponseEntity.ok(Responses.ok("Comment deleted", null));
    }
}
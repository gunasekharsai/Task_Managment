package com.taskmanagment.app.controller;


import com.taskmanagment.app.Dto.NotificationResponseDto;
import com.taskmanagment.app.Dto.Responses;
import com.taskmanagment.app.security.UserPrincipal;
import com.taskmanagment.app.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
// @Tag(name = "Notifications", description = "In-app notifications — list, read, mark all read")
public class NotificationController {

    private final NotificationService notificationService;

    // ── Get all notifications (paginated) ─────────────────────────────────────

    @GetMapping
    // @Operation(summary = "Get paginated notifications for the current user (newest first)")
    public ResponseEntity<Responses<Page<NotificationResponseDto>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(Responses.ok(
                notificationService.getNotifications(principal.getId(), pageable)));
    }

    // ── Get unread count ──────────────────────────────────────────────────────

    @GetMapping("/unread-count")
    // @Operation(summary = "Get the number of unread notifications")
    public ResponseEntity<Responses<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {

        long count = notificationService.countUnread(principal.getId());
        return ResponseEntity.ok(Responses.ok(Map.of("unreadCount", count)));
    }

    // ── Mark single notification as read ──────────────────────────────────────

    @PatchMapping("/{notificationId}/read")
    // @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<Responses<Void>> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String notificationId) {

        notificationService.markAsRead(notificationId, principal.getId());
        return ResponseEntity.ok(Responses.ok("Notification marked as read", null));
    }

    // ── Mark all notifications as read ────────────────────────────────────────

    @PatchMapping("/read-all")
    // @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Responses<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {

        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.ok(Responses.ok("All notifications marked as read", null));
    }
}
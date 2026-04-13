package com.taskmanagment.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.taskmanagment.app.Dto.NotificationResponseDto;
import com.taskmanagment.app.Models.Notification;

public interface NotificationService {
    void createAndSend(String recipientId, Notification.Type type,
                       String message, String referenceId, String referenceType);
    Page<NotificationResponseDto> getNotifications(String userId, Pageable pageable);
    long countUnread(String userId);
    void markAsRead(String notificationId, String userId);
    void markAllAsRead(String userId);
}
 
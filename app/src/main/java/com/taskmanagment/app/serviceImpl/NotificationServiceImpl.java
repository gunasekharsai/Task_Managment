package com.taskmanagment.app.serviceImpl;



import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskmanagment.app.Dto.NotificationResponseDto;
import com.taskmanagment.app.Exceptions.AccessDeniedException;
import com.taskmanagment.app.Exceptions.ResourceNotFoundException;
import com.taskmanagment.app.Models.Notification;
import com.taskmanagment.app.Models.UserModel;
import com.taskmanagment.app.Repository.NotificationRepository;
import com.taskmanagment.app.Repository.UserRepository;
import com.taskmanagment.app.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Async
    @Transactional
    public void createAndSend(String recipientId, Notification.Type type,
                              String message, String referenceId, String referenceType) {
        UserModel recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            log.warn("Notification skipped — recipient not found: {}", recipientId);
            return;
        }

        Notification notification = Notification.builder()
            .type(type)
            .message(message)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .recipient(recipient)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponseDto dto = NotificationResponseDto.from(saved);

        // Push via WebSocket to the specific user's queue
        try {
            messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/notifications",
                dto
            );
            log.debug("Notification pushed to user [{}]: {}", recipientId, message);
        } catch (Exception e) {
            log.warn("WebSocket push failed for user [{}], persisted anyway: {}", recipientId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotifications(String userId, Pageable pageable) {
        return notificationRepository
            .findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
            .map(NotificationResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(String userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new AccessDeniedException("This notification does not belong to you");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }
}
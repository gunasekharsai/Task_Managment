package com.taskmanagment.app.Models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
    @Index(name = "idx_notification_read", columnList = "isRead")
})
@EntityListeners
(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private String message;


    private String referenceId; // e.g. taskId or commentId

    private String referenceType;

    @JoinColumn(name = "recipient_id", nullable = false)
    @ManyToOne(fetch=FetchType.LAZY)
    private UserModel recipient;


    @Builder.Default
    private boolean isRead = false;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;



    public enum  Type{
        TASK_ASSIGNED,
        TASK_UPDATED,
        COMMENT_ADDED,
        MENTIONED_IN_COMMENT,
        TEAM_INVITE,
        TEAM_JOINED
    }
}

package com.taskmanagment.app.Models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_assignee", columnList = "assignee_id"),
    @Index(name = "idx_task_team", columnList = "team_id"),
    @Index(name = "idx_task_due_date", columnList = "dueDate")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskModel {
     @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
 
    @NotBlank
    @Size(min = 1, max = 200)
    @Column(nullable = false, length = 200)
    private String title;
 
    @Size(max = 5000)
    @Column(columnDefinition = "TEXT")
    private String description;
 
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;
 
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;
 
    private LocalDate dueDate;
 
    /** User who created this task */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserModel creator;
 
    /** User this task is assigned to (optional) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserModel assignee;
 
    /** Team/project this task belongs to (optional — personal tasks have no team) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private TeamModel team;
 
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Comments> comments = new ArrayList<>();
 
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();
 
    private LocalDateTime completedAt;
 
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @LastModifiedDate
    private LocalDateTime updatedAt;
 
    public enum Status {
        OPEN, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
    }
 
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}



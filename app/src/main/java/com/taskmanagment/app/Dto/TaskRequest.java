package com.taskmanagment.app.Dto;

import java.time.LocalDate;

import com.taskmanagment.app.Models.TaskModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class TaskRequest {
 
    @Data
    public static class Create {
        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 200, message = "Title must be 1–200 characters")
        private String title;
 
        @Size(max = 5000)
        private String description;
 
        private TaskModel.Priority priority = TaskModel.Priority.MEDIUM;
 
        private LocalDate dueDate;
 
        private String assigneeId;
 
        private String teamId;
    }
 
    @Data
    public static class Update {
        @Size(min = 1, max = 200)
        private String title;
 
        @Size(max = 5000)
        private String description;
 
        private TaskModel.Status status;
 
        private TaskModel.Priority priority;
 
        private LocalDate dueDate;
 
        private String assigneeId;
    }
 
    @Data
    public static class Assign {
        @NotBlank(message = "Assignee ID is required")
        private String assigneeId;
    }
 
    @Data
    public static class GenerateDescription {
        @NotBlank(message = "Prompt is required")
        @Size(max = 500)
        private String prompt;
    }
}

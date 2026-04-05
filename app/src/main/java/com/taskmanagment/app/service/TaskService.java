package com.taskmanagment.app.service;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.taskmanagment.app.Dto.TaskRequest;
import com.taskmanagment.app.Dto.TaskResponseDto;
import com.taskmanagment.app.Models.TaskModel;

public interface TaskService {
    TaskResponseDto createTask(String creatorId, TaskRequest.Create request);
    TaskResponseDto getTask(String taskId, String requesterId);
    TaskResponseDto updateTask(String taskId, String requesterId, TaskRequest.Update request);
    void deleteTask(String taskId, String requesterId);
 
    TaskResponseDto assignTask(String taskId, String requesterId, String assigneeId);
    TaskResponseDto completeTask(String taskId, String requesterId);
 
    Page<TaskResponseDto> getMyTasks(String userId, TaskModel.Status status, Pageable pageable);
    Page<TaskResponseDto> getTeamTasks(String teamId, String requesterId, TaskModel.Status status, Pageable pageable);
    Page<TaskResponseDto> searchTasks(String keyword, String userId, String teamId, Pageable pageable);
}
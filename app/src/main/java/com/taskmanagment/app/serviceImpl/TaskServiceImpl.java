package com.taskmanagment.app.serviceImpl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskmanagment.app.Dto.TaskRequest.Create;
import com.taskmanagment.app.Dto.TaskRequest.Update;
import com.taskmanagment.app.Dto.TaskResponseDto;
import com.taskmanagment.app.Exceptions.AccessDeniedException;
import com.taskmanagment.app.Exceptions.BadRequestException;
import com.taskmanagment.app.Exceptions.ResourceNotFoundException;
import com.taskmanagment.app.Models.TaskModel;
import com.taskmanagment.app.Models.TaskModel.Status;
import com.taskmanagment.app.Models.TeamModel;
import com.taskmanagment.app.Models.UserModel;
import com.taskmanagment.app.Repository.TaskRepository;
import com.taskmanagment.app.Repository.TeamRepository;
import com.taskmanagment.app.Repository.UserRepository;
import com.taskmanagment.app.service.TaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class TaskServiceImpl implements TaskService{

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;


    // create task
    @Override
    @Transactional
    public TaskResponseDto createTask(String creatorId, Create request) {
        UserModel user = userRepository.findById(creatorId).orElseThrow(() -> new RuntimeException("User not found"));

        TaskModel task =  TaskModel.builder()
        .creator(user).
        title(request.getTitle())
        .description(request.getDescription())
        .priority(request.getPriority() != null ? request.getPriority() : TaskModel.Priority.MEDIUM)
        .dueDate(request.getDueDate())
        .status(Status.OPEN)
        .build();

         // Optional: assign to a team
        if (request.getTeamId() != null) {
            TeamModel team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getTeamId()));
            assertTeamMember(team, creatorId);
            task.setTeam(team);
        }

         // Optional: assign to another user
        if (request.getAssigneeId() != null) {
            UserModel assignee = findUser(request.getAssigneeId());
            task.setAssignee(assignee);
        }



       TaskModel saved = taskRepository.save(task);
        log.info("Task created [{}] by user [{}]", saved.getId(), creatorId);

        return TaskResponseDto.from(saved);
    }

    // read

    @Override
    public TaskResponseDto getTask(String taskId, String requesterId) {
        TaskModel task = findTask(taskId);
        assertCanAccessTask(task, requesterId);
        return TaskResponseDto.from(task);
    }

    @Override
    @Transactional
    public TaskResponseDto updateTask(String taskId, String requesterId, Update request) {
        // TODO Auto-generated method stub
        TaskModel task = findTask(taskId);
        assertCanModifyTask(task, requesterId);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
       if (request.getStatus() != null && request.getStatus() != task.getStatus()) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskModel.Status.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }
 
        if (request.getAssigneeId() != null) {
            UserModel newAssignee = findUser(request.getAssigneeId());
            boolean assigneeChanged = task.getAssignee() == null
                || !task.getAssignee().getId().equals(request.getAssigneeId());
 
            task.setAssignee(newAssignee);
        }   

        taskRepository.save(task);
        log.info("Task updated [{}] by user [{}]", taskId, requesterId);

        return TaskResponseDto.from(task);
    }

    @Override
    @Transactional
    public void deleteTask(String taskId, String requesterId) {
        TaskModel task = findTask(taskId);
        assertCanModifyTask(task, requesterId);
        taskRepository.delete(task);
        log.info("Task deleted [{}] by user [{}]", taskId, requesterId);
   }

    @Override
    @Transactional
    public TaskResponseDto assignTask(String taskId, String requesterId, String assigneeId) {
        // TODO Auto-generated method stub   
        TaskModel task = findTask(taskId);
        assertCanModifyTask(task, requesterId);
 
        UserModel assignee = findUser(assigneeId);
        task.setAssignee(assignee);
        TaskModel saved = taskRepository.save(task);   
        log.info("Task [{}] assigned to user [{}] by requester [{}]", taskId, assigneeId, requesterId);
        return TaskResponseDto.from(saved);
     }

    @Override
    @Transactional
    public TaskResponseDto completeTask(String taskId, String requesterId) {
        // TODO Auto-generated method stub
        TaskModel task = findTask(taskId);
        assertCanAccessTask(task, requesterId);

        if (task.getStatus() == TaskModel.Status.COMPLETED) {
            throw new BadRequestException("Task is already completed");
        }
 
        task.setStatus(TaskModel.Status.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        TaskModel saved = taskRepository.save(task);
        log.info("Task [{}] marked as completed by user [{}]", taskId, requesterId);
        return TaskResponseDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getMyTasks(String userId, Status status, Pageable pageable) {
      Page<TaskModel> page = (status != null)
            ? taskRepository.findByAssigneeIdAndStatus(userId, status, pageable)
            : taskRepository.findByAssigneeId(userId, pageable);
        return page.map(TaskResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTeamTasks(String teamId, String requesterId, Status status, Pageable pageable) {
       TeamModel team = teamRepository.findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        assertTeamMember(team, requesterId);
 
        Page<TaskModel> page = (status != null)
            ? taskRepository.findByTeamIdAndStatus(teamId, status, pageable)
            : taskRepository.findByTeamId(teamId, pageable);
        return page.map(TaskResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> searchTasks(String keyword, String userId, String teamId, Pageable pageable) {
        // TODO Auto-generated method stub
        return taskRepository.searchTasks(keyword, userId, teamId, pageable)
            .map(TaskResponseDto::from);   
 }


    //helper methods

     private TaskModel findTask(String taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
    }
 
    private UserModel findUser(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private void assertCanAccessTask(TaskModel task, String userId) {
        boolean isCreator  = task.getCreator().getId().equals(userId);
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(userId);
        boolean isTeamMember = task.getTeam() != null
            && task.getTeam().getMembers().stream().anyMatch(m -> m.getId().equals(userId));
 
        if (!isCreator && !isAssignee && !isTeamMember) {
            throw new AccessDeniedException("You do not have access to this task");
        }
    }


     /** Only creator or team owner can modify */
    private void assertCanModifyTask(TaskModel task, String userId) {
        boolean isCreator = task.getCreator().getId().equals(userId);
        boolean isTeamOwner = task.getTeam() != null
            && task.getTeam().getOwner().getId().equals(userId);
 
        if (!isCreator && !isTeamOwner) {
            throw new AccessDeniedException("You do not have permission to modify this task");
        }
    }

    private void assertTeamMember(TeamModel team, String userId) {
        boolean isMember = team.getMembers().stream().anyMatch(m -> m.getId().equals(userId))
            || team.getOwner().getId().equals(userId);
        if (!isMember) {
            throw new AccessDeniedException("You are not a member of this team");
        }
    }
    
}

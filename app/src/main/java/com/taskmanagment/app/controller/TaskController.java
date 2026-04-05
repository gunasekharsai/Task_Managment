package com.taskmanagment.app.controller;



// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanagment.app.Dto.Responses;
import com.taskmanagment.app.Dto.TaskRequest;
import com.taskmanagment.app.Dto.TaskResponseDto;
import com.taskmanagment.app.Models.TaskModel;
import com.taskmanagment.app.security.UserPrincipal;
import com.taskmanagment.app.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
// @Tag(name = "Tasks", description = "Task management — create, read, update, delete, assign, search")
public class TaskController {

    private final TaskService taskService;
    // private final AiTaskService aiTaskService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @PostMapping
    // @Operation(summary = "Create a new task")
    public ResponseEntity<Responses<TaskResponseDto>> createTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TaskRequest.Create request) {
        TaskResponseDto task = taskService.createTask(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Responses.ok("Task created", task));
    }

    @GetMapping("/{taskId}")
    // @Operation(summary = "Get a task by ID")
    public ResponseEntity<Responses<TaskResponseDto>> getTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String taskId) {
        return ResponseEntity.ok(Responses.ok(taskService.getTask(taskId, principal.getId())));
    }

    @PutMapping("/{taskId}")
    // @Operation(summary = "Update a task")
    public ResponseEntity<Responses<TaskResponseDto>> updateTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String taskId,
            @Valid @RequestBody TaskRequest.Update request) {
        return ResponseEntity.ok(Responses.ok(
            "Task updated", taskService.updateTask(taskId, principal.getId(), request)));
    }

    @DeleteMapping("/{taskId}")
    // @Operation(summary = "Delete a task")
    public ResponseEntity<Responses<Void>> deleteTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String taskId) {
        taskService.deleteTask(taskId, principal.getId());
        return ResponseEntity.ok(Responses.ok("Task deleted", null));
    }

    // ── Assign & Complete ─────────────────────────────────────────────────────

    @PatchMapping("/{taskId}/assign")
    // @Operation(summary = "Assign a task to a team member")
    public ResponseEntity<Responses<TaskResponseDto>> assignTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String taskId,
            @Valid @RequestBody TaskRequest.Assign request) {
        return ResponseEntity.ok(Responses.ok(
            "Task assigned", taskService.assignTask(taskId, principal.getId(), request.getAssigneeId())));
    }

    @PatchMapping("/{taskId}/complete")
    // @Operation(summary = "Mark a task as completed")
    public ResponseEntity<Responses<TaskResponseDto>> completeTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String taskId) {
        return ResponseEntity.ok(Responses.ok(
            "Task marked as completed", taskService.completeTask(taskId, principal.getId())));
    }

    // ── List / Filter / Search ────────────────────────────────────────────────

    @GetMapping("/my")
    // @Operation(summary = "Get tasks assigned to me, optionally filtered by status")
    public ResponseEntity<Responses<Page<TaskResponseDto>>> getMyTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) TaskModel.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                                       : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(Responses.ok(taskService.getMyTasks(principal.getId(), status, pageable)));
    }

    @GetMapping("/team/{teamId}")
    // @Operation(summary = "Get tasks for a team, optionally filtered by status")
    public ResponseEntity<Responses<Page<TaskResponseDto>>> getTeamTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String teamId,
            @RequestParam(required = false) TaskModel.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                                       : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(Responses.ok(
            taskService.getTeamTasks(teamId, principal.getId(), status, pageable)));
    }

    @GetMapping("/search")
    // @Operation(summary = "Search tasks by keyword in title or description")
    public ResponseEntity<Responses<Page<TaskResponseDto>>> searchTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String keyword,
            @RequestParam(required = false) String teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(Responses.ok(
            taskService.searchTasks(keyword, principal.getId(), teamId, pageable)));
    }

    // ── AI Description Generation ─────────────────────────────────────────────

    // @PostMapping("/generate-description")
    // // @Operation(summary = "Use AI to generate a task description from a short prompt")
    // public ResponseEntity<Responses<Map<String, String>>> generateDescription(
    //         @Valid @RequestBody TaskRequest.GenerateDescription request) {
    //     String description = aiTaskService.generateTaskDescription(request.getPrompt());
    //     return ResponseEntity.ok(ApiResponseDto.ok(Map.of("description", description)));
    // }
}

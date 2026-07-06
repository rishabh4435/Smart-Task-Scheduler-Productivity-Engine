package com.thakur.scheduler.task.controller;

import com.thakur.scheduler.task.dto.request.DependencyRequestDto;
import com.thakur.scheduler.task.dto.request.StatusUpdateRequest;
import com.thakur.scheduler.task.dto.request.TaskCreateRequestDto;
import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import com.thakur.scheduler.task.security.CustomUserDetails;
import com.thakur.scheduler.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
@Tag(name = "2. Task Management", description = "Core APIs for creating, updating, linking, and fetching tasks")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Create a new scheduled task",
            description = "Creates a new task with a specific priority and deadline. Dependency IDs must exist in the database."
    )
    @PostMapping("/create-task")
    public ResponseEntity<TaskResponseDto> createTask
            (@Valid @RequestBody TaskCreateRequestDto taskCreateRequestDto,
             @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        String userId = customUserDetails.getUser().getId();
        return new ResponseEntity<>(taskService.createTask(taskCreateRequestDto, userId), HttpStatus.CREATED);
    }

    @PatchMapping("/update-status/{taskId}")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable String taskId
            ){
        String userId = customUserDetails.getUser().getId();
        return new ResponseEntity<>(taskService.updateTaskStatus(taskId,request.getStatus(),userId),HttpStatus.OK);
    }

    @GetMapping("/get-tasks")
    public ResponseEntity<Page<TaskResponseDto>> getTasks(
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                           Pageable pageable){
        String userId = customUserDetails.getUser().getId();
        return ResponseEntity.ok(taskService.getTasks(userId, pageable));
    }

    @PatchMapping("/{taskId}/dependencies")
    public ResponseEntity<TaskResponseDto> addDependency(
            @PathVariable String taskId,
            @Valid @RequestBody DependencyRequestDto request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        String userId = customUserDetails.getUser().getId();
        return ResponseEntity.ok(taskService.addDependency(taskId,request.getDependencyId(),userId));
    }

    @DeleteMapping("/{taskId}/dependencies/{dependencyTaskId}")
    public ResponseEntity<TaskResponseDto> deleteTask
            (@AuthenticationPrincipal CustomUserDetails customUserDetails,
             @PathVariable String taskId,
             @PathVariable String dependencyTaskId){
        String userId = customUserDetails.getUser().getId();

        return ResponseEntity.ok(taskService.removeDependency(taskId,dependencyTaskId,userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByStatus(
            @PathVariable Status status,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String userId = customUserDetails.getUser().getId();
        return ResponseEntity.ok(taskService.getTasksByStatus(userId,status));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByPriority
            (@PathVariable Priority priority,
             @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String userId = customUserDetails.getUser().getId();
        return ResponseEntity.ok(taskService.getTasksByPriority(userId,priority));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask
            (@AuthenticationPrincipal CustomUserDetails customUserDetails,
             @PathVariable String taskId){
        String userId = customUserDetails.getUser().getId();
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.noContent().build();
    }


}

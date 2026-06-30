package com.thakur.scheduler.task.service;

import com.thakur.scheduler.task.dto.request.TaskCreateRequestDto;
import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.exception.BadRequestException;
import com.thakur.scheduler.task.exception.ResourceNotFoundException;
import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import com.thakur.scheduler.task.repository.TaskRepository;
import com.thakur.scheduler.task.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponseDto createTask(TaskCreateRequestDto request, String userId) {
        validateUserExists(userId);

        Task task = Task.builder()
                .userId(userId)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .priority(request.getPriority())
                .estimatedHours(request.getEstimatedHours())
                .deadline(request.getDeadline())
                .dependencies(request.getDependencies() == null ? new HashSet<>() : request.getDependencies())
                .status(Status.PENDING)
                .created(Instant.now())
                .build();

        Task saved = taskRepository.save(task);
        return mapToDto(saved);
    }

    @Transactional
    public TaskResponseDto updateTaskStatus(String taskId, Status status, String userId) {
        validateUserExists(userId);

        Task task = getTaskOrThrow(taskId, userId);
        task.setStatus(status);

        if (status == Status.COMPLETED) {
            task.setCompletedAt(Instant.now());
        }

        Task saved = taskRepository.save(task);
        return mapToDto(saved);
    }

    public List<TaskResponseDto> getTasks(String userId) {
        validateUserExists(userId);

        return taskRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public TaskResponseDto addDependency(String taskId, String dependencyTaskId, String userId) {
        validateUserExists(userId);

        if (taskId.equals(dependencyTaskId)) {
            throw new BadRequestException("Task cannot depend on itself");
        }

        Task task = getTaskOrThrow(taskId, userId);
        getTaskOrThrow(dependencyTaskId, userId);

        if (task.getDependencies() == null) {
            task.setDependencies(new HashSet<>());
        }

        task.getDependencies().add(dependencyTaskId);

        Task saved = taskRepository.save(task);
        return mapToDto(saved);
    }

    public TaskResponseDto removeDependency(String taskId, String dependencyTaskId, String userId) {
        validateUserExists(userId);

        Task task = getTaskOrThrow(taskId, userId);

        if (task.getDependencies() != null) {
            task.getDependencies().remove(dependencyTaskId);
        }

        Task saved = taskRepository.save(task);
        return mapToDto(saved);
    }

    public List<TaskResponseDto> getTasksByStatus(String userId, Status status) {
        log.info("🔍 Searching for tasks with userId: '{}' and status: '{}'", userId, status);
        validateUserExists(userId);

        return taskRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<TaskResponseDto> getTasksByPriority(String userId, Priority priority) {
        validateUserExists(userId);

        return taskRepository.findByUserIdAndPriority(userId, priority)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private void validateUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private Task getTaskOrThrow(String taskId, String userId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private TaskResponseDto mapToDto(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDeadline(),
                task.getDependencies()
        );
    }


    @Transactional
    public void deleteTask(String taskId, String userId) {
        validateUserExists(userId);
        Task task = taskRepository.findByUserIdAndDeletedAtIsNull(taskId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        List<Task> dependentTasks = taskRepository.findByUserIdAndDependenciesContaining(taskId,userId);
        dependentTasks.forEach(t -> t.getDependencies().remove(taskId));
        taskRepository.saveAll(dependentTasks);
        taskRepository.delete(task);
    }
}
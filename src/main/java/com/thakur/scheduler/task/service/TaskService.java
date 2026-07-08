package com.thakur.scheduler.task.service;

import com.thakur.scheduler.audit.AuditAction;
import com.thakur.scheduler.audit.AuditLogEvent;
import com.thakur.scheduler.audit.EntityType;
import com.thakur.scheduler.core.algorithm.CycleDetector;
import com.thakur.scheduler.core.graph.GraphBuilder;
import com.thakur.scheduler.core.mapper.EngineTaskMapper;
import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.core.model.TaskGraph;
import com.thakur.scheduler.task.dto.request.TaskCreateRequestDto;
import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.exception.BadRequestException;
import com.thakur.scheduler.task.exception.DuplicateResourceException;
import com.thakur.scheduler.task.exception.ResourceNotFoundException;
import com.thakur.scheduler.task.mapper.TaskMapper;
import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import com.thakur.scheduler.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.thakur.scheduler.task.model.enums.Status.COMPLETED;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;


    private final ApplicationEventPublisher eventPublisher;

    @EvictSchedulerCaches
    @Transactional
    public TaskResponseDto createTask(TaskCreateRequestDto request, String userId) {

        log.info("Creating task for user {}", userId);

        validateDependencies(request.getDependencies(), userId);

        if (taskRepository.existsByUserIdAndTitleAndDeletedAtIsNull(
                userId,
                request.getTitle().trim())) {
            throw new DuplicateResourceException("Task with this title already exists.");
        }

        Task task = Task.builder()
                .userId(userId)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .priority(request.getPriority())
                .estimatedHours(request.getEstimatedHours())
                .deadline(request.getDeadline())
                .dependencies(request.getDependencies() == null
                        ? new HashSet<>()
                        : new HashSet<>(request.getDependencies()))
                .status(Status.PENDING)
                .created(Instant.now())
                .build();

        try {

            Task savedTask = taskRepository.save(task);


            eventPublisher.publishEvent(new AuditLogEvent(
                    userId,
                    AuditAction.CREATE_TASK,
                    EntityType.TASK,
                    savedTask.getId(),
                    true,
                    "Created task '" + savedTask.getTitle() + "'"
            ));

            log.info("Task {} created successfully", savedTask.getId());
            return TaskMapper.toResponseDto(savedTask);

        } catch (org.springframework.dao.DuplicateKeyException ex) {
            log.warn("Race condition prevented: Duplicate task title '{}' intercepted for user {}", request.getTitle(), userId);
            throw new DuplicateResourceException("Task with this title already exists.");

        } catch (Exception ex) {
            log.error("Failed to create task for user {}", userId, ex);


            eventPublisher.publishEvent(new AuditLogEvent(
                    userId, AuditAction.CREATE_TASK, EntityType.TASK, null, false, ex.getMessage()
            ));

            throw ex;
        }
    }

    @EvictSchedulerCaches
    @Transactional
    public TaskResponseDto updateTaskStatus(String taskId, Status status, String userId) {

        Task task = getTaskOrThrow(taskId, userId);
        task.setStatus(status);

        if(status == COMPLETED)
            task.setCompletedAt(Instant.now());
        else
            task.setCompletedAt(null);

        Task saved = taskRepository.save(task);


        eventPublisher.publishEvent(new AuditLogEvent(
                userId,
                AuditAction.UPDATE_TASK,
                EntityType.TASK,
                saved.getId(),
                true,
                "Status changed to " + saved.getStatus()
        ));

        return TaskMapper.toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasks(String userId, Pageable pageable) {

        return taskRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
                .map(TaskMapper::toResponseDto);
    }

    @EvictSchedulerCaches
    @Transactional
    public TaskResponseDto addDependency(String taskId, String dependencyTaskId, String userId) {

        if (taskId.equals(dependencyTaskId)) {
            throw new BadRequestException("Task cannot depend on itself");
        }

        Task task = getTaskOrThrow(taskId, userId);
        getTaskOrThrow(dependencyTaskId, userId);

        if (task.getDependencies() != null && task.getDependencies().contains(dependencyTaskId)) {
            throw new BadRequestException("Task already has this dependency");
        }

        if (wouldCreateCycle(taskId, dependencyTaskId, userId)) {
            throw new BadRequestException("Adding this dependency would create a cycle");
        }

        if (task.getDependencies() == null) {
            task.setDependencies(new HashSet<>());
        }
        task.getDependencies().add(dependencyTaskId);

        Task saved = taskRepository.save(task);


        eventPublisher.publishEvent(new AuditLogEvent(
                userId,
                AuditAction.ADD_DEPENDENCY,
                EntityType.TASK,
                task.getId(),
                true,
                "Added dependency " + dependencyTaskId
        ));

        return TaskMapper.toResponseDto(saved);
    }

    @Transactional
    @EvictSchedulerCaches
    public TaskResponseDto removeDependency(String taskId, String dependencyTaskId, String userId) {

        Task task = getTaskOrThrow(taskId, userId);

        if (task.getDependencies() == null || !task.getDependencies().contains(dependencyTaskId)) {
            throw new BadRequestException("Dependency is not present on this task.");
        }

        task.getDependencies().remove(dependencyTaskId);


        Task saved = taskRepository.save(task);


        eventPublisher.publishEvent(new AuditLogEvent(
                userId,
                AuditAction.REMOVE_DEPENDENCY,
                EntityType.TASK,
                task.getId(),
                true,
                "Removed dependency " + dependencyTaskId
        ));

        return TaskMapper.toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByStatus(String userId, Status status) {
        log.info("🔍 Searching for tasks with userId: '{}' and status: '{}'", userId, status);

        return taskRepository.findByUserIdAndDeletedAtIsNullAndStatus(userId, status)
                .stream()
                .map(TaskMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByPriority(String userId, Priority priority) {

        return taskRepository.findByUserIdAndDeletedAtIsNullAndPriority(userId, priority)
                .stream()
                .map(TaskMapper::toResponseDto)
                .toList();
    }

    @EvictSchedulerCaches
    @Transactional
    public void deleteTask(String taskId, String userId) {
        Task task = getTaskOrThrow(taskId, userId);
        List<Task> dependentTasks = taskRepository.findByUserIdAndDependenciesContaining(userId,taskId);
        dependentTasks.forEach(task1 -> {
            if(task1.getDependencies()!=null){
                task1.getDependencies().remove(taskId);
            }
        });
        taskRepository.saveAll(dependentTasks);
        task.setDeletedAt(Instant.now());
        task.setStatus(Status.DELETED);
        taskRepository.save(task);


        eventPublisher.publishEvent(new AuditLogEvent(
                userId,
                AuditAction.DELETE_TASK,
                EntityType.TASK,
                task.getId(),
                true,
                "Soft deleted task"
        ));
    }

    private Task getTaskOrThrow(String taskId, String userId) {
        return taskRepository
                .findByIdAndUserIdAndDeletedAtIsNull(taskId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found")
                );
    }

    private void validateDependencies(Set<String> dependencies, String userId) {
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        long validCount = taskRepository.countByIdInAndUserIdAndDeletedAtIsNull(dependencies, userId);

        if (validCount != dependencies.size()) {
            throw new BadRequestException("One or more dependency tasks are invalid, deleted, or do not belong to you.");
        }
    }

    private boolean wouldCreateCycle(String taskId, String dependencyTaskId, String userId) {
        List<Task> userTasks = taskRepository.findByUserIdAndDeletedAtIsNull(userId);
        List<EngineTask> engineTasks = userTasks.stream()
                .map(EngineTaskMapper::toEngineTask)
                .toList();
        List<EngineTask> modifiedTasks = new ArrayList<>();
        for (EngineTask et : engineTasks) {
            if (et.id().equals(taskId)) {
                Set<String> newDeps = new HashSet<>(et.dependencies());
                newDeps.add(dependencyTaskId);
                et = new EngineTask(et.id(), et.priorityWeight(), et.estimateHours(),
                        et.deadline(), newDeps);
            }
            modifiedTasks.add(et);
        }
        TaskGraph graph = GraphBuilder.build(modifiedTasks);

        return CycleDetector.detectCycle(graph);
    }
}
package com.thakur.scheduler.task.service;

import com.thakur.scheduler.audit.AuditService;
import com.thakur.scheduler.task.dto.request.TaskCreateRequestDto;
import com.thakur.scheduler.task.exception.BadRequestException;
import com.thakur.scheduler.task.exception.DuplicateResourceException;
import com.thakur.scheduler.task.exception.ResourceNotFoundException;
import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import com.thakur.scheduler.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private AuditService auditService;
    @InjectMocks private TaskService taskService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    void testCreateTask_DuplicateTitleThrowsException() {
        TaskCreateRequestDto request = new TaskCreateRequestDto("Duplicate Title", "Desc", Priority.HIGH, 2, Instant.now(), Set.of());
        String userId = "user1";

        when(taskRepository.existsByUserIdAndTitleAndDeletedAtIsNull(userId, request.getTitle().trim())).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> taskService.createTask(request, userId)
        );
        verify(taskRepository, never()).save(any());

    }


    @Test
    void testAddDependency_CycleDetected() {
        String userId = "user1";

        Task task1 = Task.builder().id("T1").userId(userId).dependencies(new HashSet<>(Set.of("T2"))).build();
        Task task2 = Task.builder().id("T2").userId(userId).dependencies(new HashSet<>()).build();

        when(taskRepository.findByIdAndUserIdAndDeletedAtIsNull("T2", userId)).thenReturn(Optional.of(task2));
        when(taskRepository.findByIdAndUserIdAndDeletedAtIsNull("T1", userId)).thenReturn(Optional.of(task1));

        when(taskRepository.findByUserIdAndDeletedAtIsNull(userId)).thenReturn(List.of(task1, task2));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.addDependency("T2", "T1", userId)
        );

        assertTrue(exception.getMessage().contains("Adding this dependency would create a cycle"));
    }

    @Test
    void testRemoveDependency() {
        String userId = "user1";

        Task task1 = Task.builder().id("T1").userId(userId).dependencies(new HashSet<>(Set.of("T2"))).build();

        when(taskRepository.findByIdAndUserIdAndDeletedAtIsNull("T1", userId)).thenReturn(Optional.of(task1));

        when(taskRepository.save(any(Task.class))).thenReturn(task1);

        taskService.removeDependency("T1", "T2", userId);

        assertFalse(task1.getDependencies().contains("T2"));
        verify(taskRepository, times(1)).save(task1);
    }

    @Test
    void testCreateTask_InvalidDependencyThrowsException() {
        String userId = "user1";
        TaskCreateRequestDto request = new TaskCreateRequestDto(
                "New Task", "Desc", Priority.HIGH, 2, Instant.now(), Set.of("INVALID_DEP_ID")
        );

        when(taskRepository.countByIdInAndUserIdAndDeletedAtIsNull(
                Set.of("INVALID_DEP_ID"),
                userId))
                .thenReturn(0L);


        assertThrows(
                BadRequestException.class,
                () -> taskService.createTask(request, userId)
        );

        verify(taskRepository, never()).save(any());
    }


    @Test
    void testDeleteTask_NonExistingTaskThrowsException() {
        String userId = "user1";
        String invalidTaskId = "T999";

        when(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(invalidTaskId, userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                taskService.deleteTask(invalidTaskId, userId));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void testAddDependency_DuplicateDependencyThrowsException(){
        String userId = "user1";


        Task task1 = Task.builder().id("T1").userId(userId).dependencies(new HashSet<>(Set.of("T2"))).build();
        Task task2 = Task.builder().id("T2").userId(userId).dependencies(new HashSet<>()).build();

        when(taskRepository.findByIdAndUserIdAndDeletedAtIsNull("T1", userId)).thenReturn(Optional.of(task1));
        when(taskRepository.findByIdAndUserIdAndDeletedAtIsNull("T2", userId)).thenReturn(Optional.of(task2));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.addDependency("T1", "T2", userId)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("already") ||
                exception.getMessage().toLowerCase().contains("dependency"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void testAddDependency_SelfDependencyThrowsException() {
         String userId = "user1";


        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.addDependency("T1", "T1", userId)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("cannot") ||
                exception.getMessage().toLowerCase().contains("itself"));
    }

    @Test
    void testRemoveDependency_DependencyDoesNotExistThrowsException() {
        String userId = "user1";


        Task task =  Task.builder().id("T1").userId(userId).dependencies(new HashSet<>()).build();
        when(taskRepository.findByIdAndUserIdAndDeletedAtIsNull("T1", userId)).thenReturn(Optional.of(task));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.removeDependency("T1", "T2", userId)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("not present") ||
                exception.getMessage().toLowerCase().contains("not a dependency"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void testUpdateTaskStatus_AnotherUsersTaskThrowsException(){
        String userId = "user2";
        String taskId = "T1";

        when(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(taskId, userId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.updateTaskStatus(taskId, Status.IN_PROGRESS,userId));
        verify(taskRepository, never()).save(any());
    }
}
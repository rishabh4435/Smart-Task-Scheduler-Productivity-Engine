package com.thakur.scheduler.scheduler.service;

import com.thakur.scheduler.core.engine.SchedulerEngine;
import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.scheduler.dto.Recommendation;
import com.thakur.scheduler.scheduler.dto.RecommendationResponseDto;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private SchedulerEngine schedulerEngine;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SchedulerService schedulerService;
    @Test
    void testRecommendNextTask_Success() {
        String userId = "rishabh120";

        Task mockTask = Task.builder()
                .id("T1")
                .userId(userId)
                .title("Learn TDD")
                .status(Status.PENDING)
                .priority(Priority.HIGH)
                .estimatedHours(2)
                .build();

        EngineTask engineTask =
                new EngineTask("T1", 70, 2, Instant.now(), Set.of());

        Recommendation recommendation =
                new Recommendation(engineTask, "Testing Reason");

        when(taskRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(List.of(mockTask));

        when(schedulerEngine.recommendNextTask(any(), any()))
                .thenReturn(Optional.of(recommendation));

        // Act
        RecommendationResponseDto response =
                schedulerService.recommendNextTask(userId);

        // Assert
        assertNotNull(response);
        assertEquals("T1", response.taskId());
        assertEquals("Learn TDD", response.title());
        assertEquals("Testing Reason", response.recommendationReason());

        // Verify (AFTER execution)
        verify(taskRepository)
                .findByUserIdAndDeletedAtIsNull(userId);

        verify(schedulerEngine)
                .recommendNextTask(any(), any());
    }

    @Test
    void testRecommendNextTask_NoTasksThrowsException() {
        String userId = "user123";


        when(taskRepository.findByUserIdAndDeletedAtIsNull(userId)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> {
            schedulerService.recommendNextTask(userId);
        });
    }



}

package com.thakur.scheduler.core.engine;

import com.thakur.scheduler.core.exception.CycleDetectedException;
import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.core.planner.DailyPlanner;
import com.thakur.scheduler.core.scheduler.PriorityScheduler;
import com.thakur.scheduler.core.scheduler.ReadyTaskFinder;
import com.thakur.scheduler.scheduler.dto.Recommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class SchedulerEngineTest {

    @Mock private PriorityScheduler priorityScheduler;
    @Mock private ReadyTaskFinder readyTaskFinder;
    @Mock private DailyPlanner dailyPlanner;

    private SchedulerEngine schedulerEngine;

    @BeforeEach
    void setUp() {
        schedulerEngine = new SchedulerEngine(priorityScheduler, readyTaskFinder, dailyPlanner);
    }

    @Test
    void testRecommendNextTask_Success() {
        EngineTask taskA = new EngineTask("A", 2, 2, Instant.now(), Set.of());
        List<EngineTask> tasks = List.of(taskA);
        Set<String> completedIds = Set.of();

        when(readyTaskFinder.findReadyTasks(any(), eq(completedIds))).thenReturn(tasks);
        when(priorityScheduler.recommendNextTask(tasks))
                .thenReturn(Optional.of(new Recommendation(taskA, "Reason")));

        Optional<Recommendation> result = schedulerEngine.recommendNextTask(tasks, completedIds);

        assertTrue(result.isPresent());
        assertEquals("A", result.get().task().id());

        verify(readyTaskFinder, times(1)).findReadyTasks(any(), any());
        verify(priorityScheduler, times(1)).recommendNextTask(any());
    }

    @Test
    void testExecutionSequence_ThrowsCycleException() {

        EngineTask taskA = new EngineTask("A", 1, 1, Instant.now(), Set.of("B"));
        EngineTask taskB = new EngineTask("B", 1, 1, Instant.now(), Set.of("A"));
        List<EngineTask> cyclicTasks = List.of(taskA, taskB);

        assertThrows(CycleDetectedException.class, () -> schedulerEngine.executionSequence(cyclicTasks));
    }

    @Test
    void recommendNextTask_WhenNoReadyTasks_ReturnsEmpty() {

        EngineTask task =
                new EngineTask("A", 1, 1, Instant.now(), Set.of());

        when(readyTaskFinder.findReadyTasks(any(), any()))
                .thenReturn(List.of());

        when(priorityScheduler.recommendNextTask(any()))
                .thenReturn(Optional.empty());

        Optional<Recommendation> result =
                schedulerEngine.recommendNextTask(List.of(task), Set.of());

        assertTrue(result.isEmpty());
    }
}

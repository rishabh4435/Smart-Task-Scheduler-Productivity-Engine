package com.thakur.scheduler.core.scheduler;

import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.scheduler.dto.Recommendation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PrioritySchedulerTest {

    @Test
    void testRecommendNextTask() {
        PriorityScheduler scheduler = new PriorityScheduler(new EngineTaskComparator());
        Instant now = Instant.now();
        EngineTask taskA = new EngineTask("A", 1, 2, now.plus(2, ChronoUnit.DAYS), Set.of());
        EngineTask taskB = new EngineTask("B", 3, 2, now.plus(2, ChronoUnit.DAYS), Set.of());
        EngineTask taskC = new EngineTask("C", 3, 2, now.plus(1, ChronoUnit.DAYS), Set.of());
        Optional<Recommendation> recommendation = scheduler.recommendNextTask(List.of(taskA, taskB, taskC));
        assertTrue(recommendation.isPresent());
        assertEquals("C", recommendation.get().task().id());
    }

    @Test
    void testRecommendNextTask_EmptyList() {
        PriorityScheduler scheduler = new PriorityScheduler(new EngineTaskComparator());
        Optional<Recommendation> recommendation = scheduler.recommendNextTask(Collections.emptyList());
        assertTrue(recommendation.isEmpty(), "Scheduler should return Optional.empty() when given an empty list.");
    }
}
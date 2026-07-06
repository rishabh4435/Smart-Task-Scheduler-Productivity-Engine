package com.thakur.scheduler.core.mapper;

import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.enums.Priority;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EngineTaskMapperTest {

    @Test
    void testToEngineTask_Success() {
        Task task = new Task();
        task.setId("T1");
        task.setPriority(Priority.HIGH);
        task.setEstimatedHours(5);
        task.setDeadline(Instant.now());
        task.setDependencies(Set.of("T2"));

        EngineTask engineTask = EngineTaskMapper.toEngineTask(task);

        assertEquals("T1", engineTask.id());
        assertEquals(70, engineTask.priorityWeight(), "Priority HIGH should correctly map to weight 70");
        assertEquals(5, engineTask.estimateHours());
        assertEquals(1, engineTask.dependencies().size());
        assertTrue(engineTask.dependencies().contains("T2"));
    }

    @Test
    void testToEngineTask_NullFallbacks() {
        Task task = new Task();
        task.setId("T2");
        task.setDeadline(Instant.now());
        EngineTask engineTask = EngineTaskMapper.toEngineTask(task);
        assertEquals("T2", engineTask.id());
        assertEquals(1, engineTask.priorityWeight(), "Null priority should default to weight 10");
        assertEquals(1, engineTask.estimateHours(), "Null estimated hours should default to 1");
        assertTrue(engineTask.dependencies().isEmpty(), "Null dependencies should map to an empty Set");
    }
}
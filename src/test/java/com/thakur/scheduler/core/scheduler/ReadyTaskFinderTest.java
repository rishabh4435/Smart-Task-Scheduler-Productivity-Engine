package com.thakur.scheduler.core.scheduler;

import com.thakur.scheduler.core.graph.GraphBuilder;
import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.core.model.TaskGraph;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReadyTaskFinderTest {

    @Test
    void testFindReadyTasks() {
        ReadyTaskFinder finder = new ReadyTaskFinder();

        EngineTask taskA = new EngineTask("A", 1, 1, Instant.now(), Set.of());
        EngineTask taskB = new EngineTask("B", 1, 1, Instant.now(), Set.of("A"));
        EngineTask taskC = new EngineTask("C", 1, 1, Instant.now(), Set.of("A", "B"));
        TaskGraph graph = GraphBuilder.build(List.of(taskA, taskB, taskC));
        List<EngineTask> ready1 = finder.findReadyTasks(graph, Set.of());
        assertEquals(1, ready1.size());
        assertEquals("A", ready1.get(0).id(), "Initial state: Only A should be ready");
        List<EngineTask> ready2 = finder.findReadyTasks(graph, Set.of("A"));
        assertEquals(1, ready2.size());
        assertEquals("B", ready2.get(0).id(), "After A completes: B should be ready");
        List<EngineTask> ready3 = finder.findReadyTasks(graph, Set.of("A", "B"));
        assertEquals(1, ready3.size());
        assertEquals("C", ready3.get(0).id(), "After A and B complete: C should be ready");
        List<EngineTask> ready4 = finder.findReadyTasks(graph, Set.of("A", "B", "C"));
        assertTrue(ready4.isEmpty(), "After all tasks complete: Should return an empty list");
    }


}
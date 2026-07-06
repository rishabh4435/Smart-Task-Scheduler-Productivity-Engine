package com.thakur.scheduler.core.algorithm;

import com.thakur.scheduler.core.graph.GraphBuilder;
import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.core.model.TaskGraph;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;




class CycleDetectorTest {

    @Test
    void testNoCycle() {
        EngineTask t1 = new EngineTask("1", 1, 1, Instant.now(), Set.of());


        TaskGraph graph = GraphBuilder.build(List.of(t1));
        assertFalse(CycleDetector.detectCycle(graph));
    }

    @Test
    void testCycleDetected() {
        EngineTask a = new EngineTask("A", 1, 1, Instant.now(), Set.of("C"));
        EngineTask b = new EngineTask("B", 1, 1, Instant.now(), Set.of("A"));
        EngineTask c = new EngineTask("C", 1, 1, Instant.now(), Set.of("B"));

        TaskGraph graph = GraphBuilder.build(List.of(a, b, c));
        assertTrue(CycleDetector.detectCycle(graph));
    }
}

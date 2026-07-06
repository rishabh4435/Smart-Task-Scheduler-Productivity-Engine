package com.thakur.scheduler.core.graph;

import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.core.model.TaskGraph;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GraphBuilderTest {

    @Test
    void testBuildValidGraph() {
        EngineTask taskA = new EngineTask("A", 1, 2, Instant.now(), Set.of());
        EngineTask taskB = new EngineTask("B", 1, 2, Instant.now(), Set.of("A"));
        EngineTask taskC = new EngineTask("C", 1, 2, Instant.now(), Set.of("A", "B"));

        TaskGraph graph = GraphBuilder.build(List.of(taskA, taskB, taskC));


        assertEquals(0, graph.inDegree().get("A"));
        assertEquals(1, graph.inDegree().get("B"));
        assertEquals(2, graph.inDegree().get("C"));

        assertTrue(graph.adjacencyList().get("A").containsAll(Set.of("B", "C")));
        assertTrue(graph.adjacencyList().get("B").contains("C"));
    }

    @Test
    void testGhostNodeThrowsException() {
        EngineTask taskA = new EngineTask("A", 1, 2, Instant.now(), Set.of("GHOST_TASK"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GraphBuilder.build(List.of(taskA))
        );

        assertTrue(exception.getMessage().contains("depends on unknown task"));
    }

    @Test
    void testDuplicateDependency() {

        EngineTask taskA = new EngineTask("A", 1, 1, Instant.now(), Set.of());

        Set<String> dirtyDependencies = new HashSet<>();
        dirtyDependencies.add("A");
        EngineTask taskB = new EngineTask("B", 1, 1, Instant.now(), dirtyDependencies);


        TaskGraph graph = GraphBuilder.build(List.of(taskA, taskB));

        assertEquals(1, graph.inDegree().get("B"), "In-degree should be exactly 1, ignoring duplicate prerequisites.");

        assertEquals(1, graph.adjacencyList().get("A").size(), "Adjacency list should only contain one edge to B.");
        assertTrue(graph.adjacencyList().get("A").contains("B"));


        assertEquals(1, graph.dependencyMap().get("B").size(), "Dependency map should only register A once.");
    }
}
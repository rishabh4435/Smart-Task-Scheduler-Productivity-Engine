package com.thakur.scheduler.core.model;


import java.util.Map;
import java.util.Set;


public record TaskGraph(
        Map<String, Set<String>> adjacencyList,
        Map<String, Integer> inDegree,
        Map<String, Set<String>> dependencyMap,
        Map<String, EngineTask> taskLookup
) {
}

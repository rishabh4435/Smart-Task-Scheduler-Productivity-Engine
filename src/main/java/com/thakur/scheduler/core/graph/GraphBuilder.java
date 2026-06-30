package com.thakur.scheduler.core.graph;

import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.core.model.TaskGraph;

import java.util.*;

public class GraphBuilder {

    public static TaskGraph build(List<EngineTask> tasks) {
        Map<String, Set<String>> adjacencyList = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, EngineTask> taskLookUp = new HashMap<>();
        Map<String, Set<String>> dependencyMap = new HashMap<>();


        for (EngineTask task : tasks) {
            String id = task.id();
            adjacencyList.put(id, new HashSet<>());
            dependencyMap.put(id, new HashSet<>());
            inDegree.put(id, 0);
            taskLookUp.put(id, task);
        }


        
        for (EngineTask task : tasks) {
            String currentId = task.id();
            Set<String> prerequisites = task.dependencies();


                for (String depId : prerequisites) {

                    if (!taskLookUp.containsKey(depId)) {
                        throw new IllegalArgumentException(
                                "Invalid Graph: Task " + currentId + " depends on unknown task " + depId
                        );
                    }

                    adjacencyList.get(depId).add(currentId);
                    inDegree.merge(currentId, 1, Integer::sum);
                    dependencyMap.get(currentId).add(depId);
                }

        }

        Map<String, Set<String>> immutableAdjList = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : adjacencyList.entrySet()) {
            immutableAdjList.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }

        Map<String, Set<String>> immutableDepMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : dependencyMap.entrySet()) {
            immutableDepMap.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }

        return new TaskGraph(
                Map.copyOf(immutableAdjList),
                Map.copyOf(inDegree),
                Map.copyOf(immutableDepMap),
                Map.copyOf(taskLookUp)
        );
    }
}

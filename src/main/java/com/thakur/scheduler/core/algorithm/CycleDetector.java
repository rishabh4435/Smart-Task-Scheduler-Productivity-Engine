package com.thakur.scheduler.core.algorithm;

import com.thakur.scheduler.core.model.TaskGraph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CycleDetector {
    private CycleDetector() {
    }


    public static boolean detectCycle(TaskGraph taskGraph) {
        Map<String, Set<String>> adjacencyList = taskGraph.adjacencyList();

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for(String nodeId: adjacencyList.keySet()) {
            if(hasCycleDFS(nodeId,taskGraph,visited,recursionStack)){
                return true;
            }
        }
        return false;
    }

    private static boolean hasCycleDFS(String nodeId, TaskGraph taskGraph, Set<String> visited, Set<String> recursionStack) {

        if(recursionStack.contains(nodeId)) return true;
        if(visited.contains(nodeId)) return false;


        visited.add(nodeId);
        recursionStack.add(nodeId);

        Set<String> neighbours = taskGraph.adjacencyList().get(nodeId);


        for (String neighbour : neighbours) {
            if (hasCycleDFS(neighbour, taskGraph, visited, recursionStack)) {
                    return true;
            }
        }

        recursionStack.remove(nodeId);
        return false;
    }

}

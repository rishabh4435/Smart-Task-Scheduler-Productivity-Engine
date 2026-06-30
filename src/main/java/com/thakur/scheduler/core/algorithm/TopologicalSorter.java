package com.thakur.scheduler.core.algorithm;

import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.core.model.TaskGraph;

import java.util.*;

public class TopologicalSorter {

    public static List<EngineTask> getExecutionOrder(TaskGraph taskGraph) {

        Map<String, Integer> inDegree = new HashMap<>(taskGraph.inDegree());

        Queue<String> queue = new ArrayDeque<>();

        List<EngineTask> executionOrder = new ArrayList<>();

        for(Map.Entry<String, Integer> entry : inDegree.entrySet()){
            if(entry.getValue() == 0){
                queue.add(entry.getKey());
            }
        }

        while(!queue.isEmpty()){
            String currentTaskId = queue.poll();

            EngineTask currentTask = taskGraph.taskLookup().get(currentTaskId);
            executionOrder.add(currentTask);
            Set<String> neighbours = taskGraph.adjacencyList().get(currentTaskId);
            for(String nextTaskId : neighbours){
                int updatedInDegree = inDegree.get(nextTaskId)-1;
                inDegree.put(nextTaskId, updatedInDegree);

                if(updatedInDegree == 0){
                    queue.add(nextTaskId);
                }
            }
        }
        if(executionOrder.size() != taskGraph.taskLookup().size()){
            throw new IllegalStateException("Cannot generate execution order. A cycle might have bypassed the DFS check!");
        }

        return executionOrder;
    }
}

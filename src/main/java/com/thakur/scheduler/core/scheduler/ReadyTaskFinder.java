package com.thakur.scheduler.core.scheduler;

import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.core.model.TaskGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ReadyTaskFinder {

    public List<EngineTask> findReadyTasks(TaskGraph graph, Set<String> completedTaskIds) {
        List<EngineTask> readyTasks = new ArrayList<>();

        for (String taskId : graph.taskLookup().keySet()) {


            if (completedTaskIds.contains(taskId)) continue;


            Set<String> prereqs = graph.dependencyMap()
                    .getOrDefault(taskId, Collections.emptySet());

            EngineTask task = graph.taskLookup().get(taskId);

            if (completedTaskIds.containsAll(prereqs)) {
                readyTasks.add(task);
            }
        }

        return readyTasks;
    }
}
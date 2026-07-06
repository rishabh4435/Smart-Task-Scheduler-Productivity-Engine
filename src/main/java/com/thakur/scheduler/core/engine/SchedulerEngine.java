package com.thakur.scheduler.core.engine;

import com.thakur.scheduler.core.algorithm.CycleDetector;
import com.thakur.scheduler.core.algorithm.TopologicalSorter;
import com.thakur.scheduler.core.exception.CycleDetectedException;
import com.thakur.scheduler.core.graph.GraphBuilder;
import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.core.model.TaskGraph;
import com.thakur.scheduler.core.planner.DailyPlanner;
import com.thakur.scheduler.core.scheduler.PriorityScheduler;
import com.thakur.scheduler.core.scheduler.ReadyTaskFinder;
import com.thakur.scheduler.scheduler.dto.Recommendation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


public class SchedulerEngine {

    private final PriorityScheduler priorityScheduler;
    private final ReadyTaskFinder readyTaskFinder;
    private final DailyPlanner dailyPlanner;

    public SchedulerEngine(PriorityScheduler priorityScheduler, ReadyTaskFinder readyTaskFinder, DailyPlanner dailyPlanner) {
        this.priorityScheduler = Objects.requireNonNull(priorityScheduler);
        this.readyTaskFinder = Objects.requireNonNull(readyTaskFinder);
        this.dailyPlanner = dailyPlanner;
    }

    public List<EngineTask> executionSequence(List<EngineTask> tasks){
        if(tasks.isEmpty()) return List.of();
        TaskGraph graph = buildValidatedGraph(tasks);
        return TopologicalSorter.getExecutionOrder(graph);
    }


    public Optional<Recommendation> recommendNextTask(List<EngineTask> tasks, Set<String> completedTaskIds){
        if (tasks.isEmpty()) {
            return Optional.empty();
        }

        TaskGraph graph = buildValidatedGraph(tasks);

        List<EngineTask> readyTasks = readyTaskFinder.findReadyTasks(
                graph,
                Objects.requireNonNull(completedTaskIds)
        );

        return priorityScheduler.recommendNextTask(readyTasks);
    }

    private TaskGraph buildValidatedGraph(List<EngineTask> tasks){
        TaskGraph graph = GraphBuilder.build(tasks);
        if(CycleDetector.detectCycle(graph)) {
            throw new CycleDetectedException(
                    "Deadlock detected. Task dependency graph contains a cycle."
            );
        }
        return graph;
    }

    public List<EngineTask> getDailyPlan(
            List<EngineTask> tasks,
            Set<String> completedTaskIds,
            int availableHours
    ) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        TaskGraph graph = buildValidatedGraph(tasks);

        List<EngineTask> readyTasks =
                readyTaskFinder.findReadyTasks(graph, completedTaskIds);

        return dailyPlanner.optimize(readyTasks, availableHours);
    }
}

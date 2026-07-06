package com.thakur.scheduler.core.scheduler;

import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.scheduler.dto.Recommendation;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public class PriorityScheduler {

    private final Comparator<EngineTask> comparator;

    public PriorityScheduler(Comparator<EngineTask> engineTaskComparator) {
        this.comparator = engineTaskComparator;
    }

    public Optional<Recommendation> recommendNextTask(List<EngineTask> readyTasks){
        if(readyTasks.isEmpty()) return Optional.empty();

        PriorityQueue<EngineTask> minHeap = new PriorityQueue<>(comparator);
        minHeap.addAll(readyTasks);

        EngineTask bestTask = minHeap.poll();

        return Optional.of(
                new Recommendation(
                        bestTask,
                        "Highest priority among all ready tasks."
                )
        );
    }
}

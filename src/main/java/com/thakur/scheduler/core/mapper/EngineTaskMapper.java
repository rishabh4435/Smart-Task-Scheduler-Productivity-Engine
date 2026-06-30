package com.thakur.scheduler.core.mapper;

import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.task.model.entity.Task;

public class EngineTaskMapper {

    public static EngineTask toEngineTask(Task task) {

        int weight = (task.getPriority() != null) ? task.getPriority().getPriorityWeight() : 1;

        return new EngineTask(
                task.getId(),
                weight,
                task.getEstimatedHours() != null ? task.getEstimatedHours() : 1,
                task.getDeadline(),
                task.getDependencies()
        );
    }
}
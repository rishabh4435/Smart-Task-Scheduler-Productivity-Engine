package com.thakur.scheduler.task.mapper;

import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.model.entity.Task;

public class TaskMapper {
    private TaskMapper() {
        /* This utility class should not be instantiated */
    }


    public static TaskResponseDto toResponseDto(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDeadline(),
                task.getDependencies()
        );
    }
}

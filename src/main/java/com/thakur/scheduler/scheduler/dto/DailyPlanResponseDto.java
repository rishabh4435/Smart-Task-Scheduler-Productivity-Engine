package com.thakur.scheduler.scheduler.dto;

import com.thakur.scheduler.task.dto.response.TaskResponseDto;

import java.util.List;

public record DailyPlanResponseDto(
        List<TaskResponseDto> tasks,

        int availableHours,

        int totalEstimatedHours
) {
}

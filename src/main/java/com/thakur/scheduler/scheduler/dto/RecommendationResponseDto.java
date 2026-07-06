package com.thakur.scheduler.scheduler.dto;

import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;

import java.io.Serializable;
import java.time.Instant;

public record RecommendationResponseDto(
        String taskId,

        String title,

        Priority priority,

        Status status,

        Instant deadline,

        Integer estimatedHours,

        String recommendationReason
) implements Serializable {
}

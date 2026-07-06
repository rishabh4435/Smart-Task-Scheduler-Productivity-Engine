package com.thakur.scheduler.task.dto.request;


import com.thakur.scheduler.task.model.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCreateRequestDto {
    @NotBlank
    @Size(max = 120)
    @Schema(example = "Master System Design Graph Algorithms")
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull
    @Schema(example = "HIGH")
    private Priority priority;

    @NotNull
    @Positive
    @Schema(example = "4", description = "Estimated time to complete in hours")
    private Integer estimatedHours;

    @FutureOrPresent
    @NotNull(message = "Deadline cannot be null")
    @Schema(
            description = "The deadline for the task in ISO-8601 format",
            example = "2026-07-08T15:30:00Z",
            type = "string",
            format = "date-time"
    )
    private Instant deadline;

    @Size(max = 50)
    private Set<String> dependencies;
}

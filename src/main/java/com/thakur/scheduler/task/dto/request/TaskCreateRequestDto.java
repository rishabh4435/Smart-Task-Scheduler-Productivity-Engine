package com.thakur.scheduler.task.dto.request;


import com.thakur.scheduler.task.model.enums.Priority;
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
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull
    private Priority priority;

    @NotNull
    @Positive
    private Integer estimatedHours;

    @FutureOrPresent
    private Instant deadline;

    @Size(max = 50)
    private Set<String> dependencies;
}

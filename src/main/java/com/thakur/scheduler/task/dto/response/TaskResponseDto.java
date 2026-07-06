package com.thakur.scheduler.task.dto.response;

import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto implements Serializable {
    private String id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private Instant deadline;
    private Set<String> dependencies;
}

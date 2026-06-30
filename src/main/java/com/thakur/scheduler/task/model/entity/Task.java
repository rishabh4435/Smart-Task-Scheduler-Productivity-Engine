package com.thakur.scheduler.task.model.entity;

import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@Document(collection = "tasks")
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    private String id;
    private String userId;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Integer estimatedHours;
    private Instant deadline;
    private Set<String> dependencies;
    @CreatedDate
    private Instant created;
    private Instant completedAt;
    private Instant deletedAt;
}

package com.thakur.scheduler.task.model.entity;

import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tasks")
@CompoundIndexes({

        @CompoundIndex(name = "user_deleted_status_idx", def = "{'userId': 1, 'deletedAt': 1, 'status': 1}"),


        @CompoundIndex(name = "user_deleted_priority_idx", def = "{'userId': 1, 'deletedAt': 1, 'priority': 1}"),

        @CompoundIndex(name = "user_deleted_title_idx", def = "{'userId': 1, 'deletedAt': 1, 'title': 1}", unique = true)
})
public class Task {
    @Id
    private String id;
    private String userId;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Integer estimatedHours;
    @Indexed
    private Instant deadline;
    private Set<String> dependencies;
    private Instant created;
    private Instant completedAt;
    private Instant deletedAt;
}

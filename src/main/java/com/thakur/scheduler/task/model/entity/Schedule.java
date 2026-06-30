package com.thakur.scheduler.task.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "schedule")
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    private String id;
    private String userId;
    private List<String> optimizedTaskIds;
    private Integer totalHoursAllocated;
    private Instant created;
}

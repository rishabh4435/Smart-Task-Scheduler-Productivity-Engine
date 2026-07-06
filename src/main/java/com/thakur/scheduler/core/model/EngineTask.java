package com.thakur.scheduler.core.model;

import java.time.Instant;
import java.util.Set;

public record EngineTask(
        String id,
        int priorityWeight,
        int estimateHours,
        Instant deadline,
        Set<String> dependencies
) {
    public EngineTask {
        dependencies = dependencies == null ? Set.of() : Set.copyOf(dependencies);
    }
}
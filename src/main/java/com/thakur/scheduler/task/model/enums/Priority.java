package com.thakur.scheduler.task.model.enums;

import lombok.Getter;

@Getter
public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);
    private final int priorityWeight;

    Priority(int weight) {
        this.priorityWeight = weight;
    }
}

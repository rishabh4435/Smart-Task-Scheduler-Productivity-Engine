package com.thakur.scheduler.task.model.enums;

import lombok.Getter;

@Getter
public enum Priority {
    LOW(10),
    MEDIUM(40),
    HIGH(70),
    CRITICAL(100);
    private final int priorityWeight;

    Priority(int weight) {
        this.priorityWeight = weight;
    }
}

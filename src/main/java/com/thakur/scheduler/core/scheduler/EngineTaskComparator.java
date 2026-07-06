package com.thakur.scheduler.core.scheduler;

import com.thakur.scheduler.core.model.EngineTask;

import java.time.Instant;
import java.util.Comparator;

public class EngineTaskComparator implements Comparator<EngineTask> {

    @Override
    public int compare(EngineTask a, EngineTask b) {
        return Comparator
                .comparingInt(EngineTask::priorityWeight)
                .reversed() // Higher priority first
                .thenComparing(
                        EngineTask::deadline,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
                .compare(a, b);
    }
}
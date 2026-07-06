package com.thakur.scheduler.scheduler.dto;

import com.thakur.scheduler.core.model.EngineTask;

public record Recommendation(
        EngineTask task,
        String reason
) {}

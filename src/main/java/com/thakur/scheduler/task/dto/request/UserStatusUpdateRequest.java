package com.thakur.scheduler.task.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    @NotNull(message = "Enabled status must be provided")
    private Boolean enabled;
}
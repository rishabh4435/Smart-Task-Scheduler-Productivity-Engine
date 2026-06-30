package com.thakur.scheduler.task.dto.request;

import com.thakur.scheduler.task.model.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotNull
    private Role role;
}

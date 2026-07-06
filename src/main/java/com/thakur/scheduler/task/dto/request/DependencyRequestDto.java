package com.thakur.scheduler.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependencyRequestDto {

    @NotBlank(message = "Dependency task ID cannot be blank")
    private String dependencyId;
}
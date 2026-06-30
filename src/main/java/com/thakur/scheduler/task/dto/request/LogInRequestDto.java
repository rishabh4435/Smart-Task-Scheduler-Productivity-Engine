package com.thakur.scheduler.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogInRequestDto {
    @NotBlank
    private String usernameOrEmail;
    @NotBlank
    private String password;
}

package com.thakur.scheduler.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequestDto {
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}
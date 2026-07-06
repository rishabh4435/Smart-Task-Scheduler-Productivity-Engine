package com.thakur.scheduler.task.dto.response;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String username,
        String role
) {}
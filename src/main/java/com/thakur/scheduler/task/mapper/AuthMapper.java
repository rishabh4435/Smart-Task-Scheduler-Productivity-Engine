package com.thakur.scheduler.task.mapper;

import com.thakur.scheduler.task.dto.response.AuthResponseDto;
import com.thakur.scheduler.task.model.entity.User;

public class AuthMapper {
    private AuthMapper() {
        /* This utility class should not be instantiated */
    }

    public static AuthResponseDto toResponseDto(String accessToken, String refreshToken, User user) {
        return new AuthResponseDto(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getRole().name()
        );
    }
}
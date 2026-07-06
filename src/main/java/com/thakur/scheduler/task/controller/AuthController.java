package com.thakur.scheduler.task.controller;

import com.thakur.scheduler.task.dto.request.LogInRequestDto;
import com.thakur.scheduler.task.dto.request.TokenRefreshRequestDto;
import com.thakur.scheduler.task.dto.request.UserRegistrationRequestDto;
import com.thakur.scheduler.task.dto.response.AuthResponseDto;
import com.thakur.scheduler.task.dto.response.UserResponseDto;
import com.thakur.scheduler.task.security.CustomUserDetails;
import com.thakur.scheduler.task.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "1. Authentication", description = "Endpoints for user registration, login, and secure session management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(userRegistrationRequestDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getId();
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LogInRequestDto logInRequestDto) {
        return ResponseEntity.ok(authService.login(logInRequestDto));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody TokenRefreshRequestDto request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }



}
package com.thakur.scheduler.task.controller;

import com.thakur.scheduler.task.dto.request.LogInRequestDto;
import com.thakur.scheduler.task.dto.request.UserRegistrationRequestDto;
import com.thakur.scheduler.task.dto.response.AuthResponseDto;
import com.thakur.scheduler.task.dto.response.UserResponseDto;
import com.thakur.scheduler.task.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(userRegistrationRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LogInRequestDto logInRequestDto) {
        return ResponseEntity.ok(authService.login(logInRequestDto));
    }


}

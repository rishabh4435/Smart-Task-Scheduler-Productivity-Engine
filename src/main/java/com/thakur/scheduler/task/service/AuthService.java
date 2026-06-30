package com.thakur.scheduler.task.service;

import com.thakur.scheduler.task.dto.request.LogInRequestDto;
import com.thakur.scheduler.task.dto.request.UserRegistrationRequestDto;
import com.thakur.scheduler.task.dto.response.AuthResponseDto;
import com.thakur.scheduler.task.dto.response.UserResponseDto;
import com.thakur.scheduler.task.exception.DuplicateResourceException;
import com.thakur.scheduler.task.model.entity.User;
import com.thakur.scheduler.task.repository.UserRepository;
import com.thakur.scheduler.task.security.CustomUserDetails;
import com.thakur.scheduler.task.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.thakur.scheduler.task.model.enums.Role.USER;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserResponseDto register(UserRegistrationRequestDto request) {

        if (userRepository.findByUsernameOrEmail(request.getUsername(),request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Username is already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(USER);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);

        UserResponseDto userResponseDto = new UserResponseDto();

        userResponseDto.setId(user.getId());
        userResponseDto.setUsername(user.getUsername());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setRole(user.getRole());
        userResponseDto.setCreatedAt(user.getCreatedAt());

        return userResponseDto;
    }


    public AuthResponseDto login(LogInRequestDto logInRequestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        logInRequestDto.getUsernameOrEmail(),
                        logInRequestDto.getPassword()
                )
        );




        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String token = jwtService.generateToken(user);
        return new AuthResponseDto(
                token,
                user.getUsername(),
                user.getRole().name()
        );
    }

    public List<UserResponseDto> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDto> dtos = new ArrayList<>();
        for (User user : users) {
            dtos.add(new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt(),user.isEnabled()));
        }
        return dtos;
    }
}

package com.thakur.scheduler.task.service;

import com.thakur.scheduler.audit.AuditAction;
import com.thakur.scheduler.audit.AuditLogEvent;
import com.thakur.scheduler.audit.AuditService;
import com.thakur.scheduler.audit.EntityType;
import com.thakur.scheduler.task.dto.request.LogInRequestDto;
import com.thakur.scheduler.task.dto.request.TokenRefreshRequestDto;
import com.thakur.scheduler.task.dto.request.UserRegistrationRequestDto;
import com.thakur.scheduler.task.dto.response.AuthResponseDto;
import com.thakur.scheduler.task.dto.response.UserResponseDto;
import com.thakur.scheduler.task.exception.DuplicateResourceException;
import com.thakur.scheduler.task.exception.ResourceNotFoundException;
import com.thakur.scheduler.task.exception.UnauthorizedException;
import com.thakur.scheduler.task.mapper.AuthMapper;
import com.thakur.scheduler.task.model.RefreshToken;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


import static com.thakur.scheduler.task.model.enums.Role.USER;
import org.springframework.context.ApplicationEventPublisher;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;
    private final RefreshTokenService refreshTokenService;
    private final ApplicationEventPublisher eventPublisher;

    public UserResponseDto register(UserRegistrationRequestDto request) {

        if (userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Username or email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(USER)
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);
        auditService.log(
                user.getId(),
                AuditAction.SIGNUP,
                EntityType.USER,
                user.getId(),
                true,
                "User registered"
        );

        return new UserResponseDto(
                user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole(),
        user.getCreatedAt(),
        user.isEnabled()
        );
    }


    public AuthResponseDto login(LogInRequestDto logInRequestDto) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            logInRequestDto.getUsernameOrEmail(),
                            logInRequestDto.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            assert userDetails != null;
            User user = userDetails.getUser();

            String token = jwtService.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            eventPublisher.publishEvent(new AuditLogEvent(
                    user.getId(),
                    AuditAction.LOGIN,
                    EntityType.USER,
                    user.getId(),
                    true,
                    "Login successful"
            ));

            return AuthMapper.toResponseDto(token, refreshToken.getToken(), user);

        } catch (Exception ex) {

            eventPublisher.publishEvent(new AuditLogEvent(
                    "SYSTEM",
                    AuditAction.LOGIN_FAILED,
                    EntityType.USER,
                    logInRequestDto.getUsernameOrEmail(),
                    false,
                    "Login failed for: " + logInRequestDto.getUsernameOrEmail() + " | Reason: " + ex.getMessage()
            ));

            throw ex;
        }
    }


    @Transactional
    public AuthResponseDto refreshToken(TokenRefreshRequestDto request) {

        return refreshTokenService.findByToken(request.getRefreshToken())


                .map(refreshTokenService::verifyExpiration)

                .map(oldToken -> {
                    String userId = oldToken.getUserId();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));


                    if (!user.isEnabled()) {
                        throw new UnauthorizedException("Account is disabled. Cannot refresh tokens.");
                    }

                    refreshTokenService.revokeToken(oldToken);


                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    String newAccessToken = jwtService.generateToken(user);


                    eventPublisher.publishEvent(new AuditLogEvent(
                            userId,
                            AuditAction.REFRESH_TOKEN,
                            EntityType.USER,
                            userId,
                            true,
                            "Tokens rotated successfully"
                    ));


                    return AuthMapper.toResponseDto(
                            newAccessToken,
                            newRefreshToken.getToken(),
                            user
                    );
                })
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid or not found in database!"));
    }


    @Transactional
    public void logout(String userId) {


        refreshTokenService.revokeAllUserTokens(userId);
        auditService.log(
                userId,
                AuditAction.LOGOUT,
                EntityType.USER,
                userId,
                true,
                "User successfully logged out from all devices"
        );
    }


}

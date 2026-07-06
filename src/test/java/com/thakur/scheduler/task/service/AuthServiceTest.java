package com.thakur.scheduler.task.service;

import com.thakur.scheduler.audit.AuditAction;
import com.thakur.scheduler.audit.AuditLogEvent;
import com.thakur.scheduler.audit.AuditService;
import com.thakur.scheduler.audit.EntityType;
import com.thakur.scheduler.task.dto.request.LogInRequestDto;
import com.thakur.scheduler.task.dto.request.UserRegistrationRequestDto;
import com.thakur.scheduler.task.dto.response.AuthResponseDto;
import com.thakur.scheduler.task.dto.response.UserResponseDto;
import com.thakur.scheduler.task.exception.DuplicateResourceException;
import com.thakur.scheduler.task.model.RefreshToken;
import com.thakur.scheduler.task.model.entity.User;
import com.thakur.scheduler.task.model.enums.Role;
import com.thakur.scheduler.task.repository.UserRepository;
import com.thakur.scheduler.task.security.CustomUserDetails;
import com.thakur.scheduler.task.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuditService auditService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks private AuthService authService;

    @Test
    void testSignup_Success() {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto("rishabh", "rishabh@test.com", "Password@123");

        when(userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        UserResponseDto response = authService.register(request);

        assertNotNull(response);
        assertEquals("rishabh", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditService, times(1)).log(any(), eq(AuditAction.SIGNUP), any(), any(), eq(true), anyString());
    }

    @Test
    void testSignup_DuplicateUserThrowsException() {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto("rishabh", "rishabh@test.com", "Password@123");

        when(userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail()))
                .thenReturn(Optional.of(new User())); // Simulating user exists

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_Success() {


        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token-123")
                .userId("1")
                .build();

        when(refreshTokenService.createRefreshToken("1"))
                .thenReturn(refreshToken);

        LogInRequestDto request = new LogInRequestDto();
        request.setUsernameOrEmail("rishabh");
        request.setPassword("Password@123");

        User mockUser = new User();
        mockUser.setId("1");
        mockUser.setUsername("rishabh");
        mockUser.setRole(Role.USER);

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateToken(mockUser))
                .thenReturn("mocked-jwt-token");


        AuthResponseDto response = authService.login(request);


        assertEquals("mocked-jwt-token", response.accessToken());
        assertEquals("refresh-token-123", response.refreshToken());
        assertEquals("rishabh", response.username());
        assertEquals(Role.USER.name(), response.role());


        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService)
                .generateToken(mockUser);

        verify(refreshTokenService)
                .createRefreshToken("1");


        ArgumentCaptor<AuditLogEvent> captor =
                ArgumentCaptor.forClass(AuditLogEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        AuditLogEvent event = captor.getValue();

        assertEquals("1", event.userId());
        assertEquals(AuditAction.LOGIN, event.action());
        assertEquals(EntityType.USER, event.entityType());
        assertEquals("1", event.entityId());
        assertTrue(event.success());
        assertEquals("Login successful", event.details());

        verifyNoMoreInteractions(
                auditService,
                eventPublisher,
                refreshTokenService,
                jwtService,
                authenticationManager
        );
    }
}
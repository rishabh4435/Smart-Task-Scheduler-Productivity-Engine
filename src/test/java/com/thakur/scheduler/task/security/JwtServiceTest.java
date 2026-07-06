package com.thakur.scheduler.task.security;

import com.thakur.scheduler.task.model.entity.User;
import com.thakur.scheduler.task.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User mockUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        String mockSecretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        long mockExpiration = 1000 * 60 * 60; // 1 hour

        ReflectionTestUtils.setField(jwtService, "secretKey", mockSecretKey);
        ReflectionTestUtils.setField(jwtService, "expirationTime", mockExpiration);

        mockUser = new User();
        mockUser.setId("User123");
        mockUser.setUsername("testuser");
        mockUser.setRole(Role.USER);

        userDetails = new CustomUserDetails(mockUser);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        String token = jwtService.generateToken(mockUser);
        assertNotNull(token, "Generated token should not be null");
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("testuser", extractedUsername, "Extracted username should match the user's username");
    }

    @Test
    void testIsTokenValid_Success() {
        String token = jwtService.generateToken(mockUser);

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid, "Token should be marked as valid");
    }

    @Test
    void testIsTokenValid_FailsForDifferentUser() {
        String token = jwtService.generateToken(mockUser);


        User intruder = new User();
        intruder.setUsername("hacker");
        intruder.setRole(Role.USER);
        CustomUserDetails intruderDetails = new CustomUserDetails(intruder);


        boolean isValid = jwtService.isTokenValid(token, intruderDetails);
        assertFalse(isValid, "Token should be invalid if the username does not match");
    }
}
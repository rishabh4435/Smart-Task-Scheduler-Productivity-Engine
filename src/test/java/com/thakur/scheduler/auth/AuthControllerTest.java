package com.thakur.scheduler.auth;

import com.thakur.scheduler.task.dto.request.LogInRequestDto;
import com.thakur.scheduler.task.dto.request.TokenRefreshRequestDto;
import com.thakur.scheduler.task.dto.request.UserRegistrationRequestDto;
import com.thakur.scheduler.task.model.RefreshToken;
import com.thakur.scheduler.task.model.entity.User;
import com.thakur.scheduler.task.model.enums.Role;
import com.thakur.scheduler.task.repository.RefreshTokenRepository;
import com.thakur.scheduler.task.repository.UserRepository;
import com.thakur.scheduler.task.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;


import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @Test
    void testRegisterUser_SuccessfulSignup() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "supercoder",
                "coder@test.com",
                "SecurePass@123"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("supercoder"))
                .andExpect(jsonPath("$.email").value("coder@test.com"));


        assertTrue(
                userRepository.findByUsernameOrEmail(
                        "supercoder",
                        "coder@test.com"
                ).isPresent()
        );
    }

    @Test
    void testRegisterUser_DuplicateUsername_ThrowsException() throws Exception {

        User existingUser = User.builder()
                .username("supercoder")
                .email("original@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .createdAt(Instant.now())
                .enabled(true)
                .build();
        userRepository.save(existingUser);


        UserRegistrationRequestDto duplicateRequest = new UserRegistrationRequestDto(
                "supercoder",
                "newemail@test.com",
                "SecurePass@123"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void testRegisterUser_DuplicateEmail_ThrowsException() throws Exception {

        User existingUser = User.builder()
                .username("originalcoder")
                .email("coder@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .createdAt(Instant.now())
                .enabled(true)
                .build();
        userRepository.save(existingUser);


        UserRegistrationRequestDto duplicateRequest = new UserRegistrationRequestDto(
                "newcoder",
                "coder@test.com",
                "SecurePass@123"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void testRegisterUser_InvalidPassword_FailsValidation() throws Exception {

        UserRegistrationRequestDto invalidRequest = new UserRegistrationRequestDto(
                "validuser",
                "valid@test.com",
                "short"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterUser_InvalidEmail_FailsValidation() throws Exception {

        UserRegistrationRequestDto invalidRequest = new UserRegistrationRequestDto(
                "validuser",
                "invalid-email-format",
                "SecurePass@123"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Successful() throws Exception {
        createUser();
        LogInRequestDto loginRequest =
                new com.thakur.scheduler.task.dto.request.LogInRequestDto();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testLogin_WrongPassword_ReturnsUnauthorized() throws Exception {
        User testUser = User.builder()
                .username("testuser")
                .email("test@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(testUser);

        LogInRequestDto loginRequest =
                new LogInRequestDto();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("WrongPassword@999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testRefreshToken_ReturnsNewTokens() throws Exception {

        User testUser = createUser();

        String validTokenString = "secure-refresh-uuid-123";
        RefreshToken rt =
                RefreshToken.builder()
                        .userId(testUser.getId())
                        .token(validTokenString)
                        .expiryDate(Instant.now().plusSeconds(6000))
                        .revoked(false)
                        .build();
        refreshTokenRepository.save(rt);


        TokenRefreshRequestDto refreshRequest =
                new TokenRefreshRequestDto();
        refreshRequest.setRefreshToken(validTokenString);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(org.hamcrest.Matchers.not(validTokenString)));

    }


    @Test
    void testLogout_Successful() throws Exception {

        User testUser = createUser();


        refreshTokenRepository.save(RefreshToken.builder()
                .userId(testUser.getId())
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(6000))
                .revoked(false)
                .build());


        String validJwt = jwtService.generateToken(testUser);


        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isNoContent()); // 204 No Content


        java.util.List<com.thakur.scheduler.task.model.RefreshToken> activeTokens =
                refreshTokenRepository.findByUserIdAndRevokedFalseOrderByExpiryDateAsc(testUser.getId());

        assertTrue(activeTokens.isEmpty(), "All refresh tokens for the user should be revoked!");
    }

    private User createUser() {
        User user = User.builder()
                .username("testuser")
                .email("test@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.USER)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }
}
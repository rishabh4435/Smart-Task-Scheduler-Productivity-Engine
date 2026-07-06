package com.thakur.scheduler.admin;



import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.entity.User;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Role;
import com.thakur.scheduler.task.model.enums.Status;
import com.thakur.scheduler.task.repository.RefreshTokenRepository;
import com.thakur.scheduler.task.repository.TaskRepository;
import com.thakur.scheduler.task.repository.UserRepository;
import com.thakur.scheduler.task.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TaskRepository taskRepository;

    private  String jwt(User user){
        return jwtService.generateToken(user);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void shouldReturnAllUsers_WhenAdminRequests() throws Exception{

        User admin = createAdmin();
        createNormalUser();

        mockMvc.perform(get("/api/admin/users/all-users")
                        .header("Authorization", "Bearer " + jwt(admin))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[1].username").exists());
    }

    @Test
    void shouldUpdateUserRole() throws Exception {
        User admin = createAdmin();
       User targetUser =  createNormalUser();

        Map<String, String> payload = Map.of("role", "ADMIN");

        mockMvc.perform(patch("/api/admin/users/{userId}/role", targetUser.getId())
                        .header("Authorization", "Bearer " + jwt(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));


        User updatedUser = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(Role.ADMIN, updatedUser.getRole());
    }

    @Test
    void shouldRejectChangingOwnRole() throws Exception {
        User admin = createAdmin();
        Map<String, String> payload = Map.of("role", "ADMIN");

        mockMvc.perform(patch("/api/admin/users/{userId}/role", admin.getId())
                        .header("Authorization", "Bearer " + jwt(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Admins cannot change their own role"));

        User updatedUser = userRepository.findById(admin.getId()).orElseThrow();
        assertEquals(Role.ADMIN, updatedUser.getRole());
    }


    @Test
    void shouldDisableUser() throws Exception {
        User admin = createAdmin();
        User targetUser =  createNormalUser();

        Map<String, Boolean> payload = Map.of("enabled", false);

        mockMvc.perform(patch("/api/admin/users/{userId}/status", targetUser.getId())
        .header("Authorization", "Bearer " + jwt(admin))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());


        User updatedUser = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(Role.USER, updatedUser.getRole());
    }



    @Test
    void shouldRejectDisablingSelf() throws Exception {
        User admin = createAdmin();


        Map<String, Boolean> payload = Map.of("enabled", false);

        mockMvc.perform(patch("/api/admin/users/{userId}/role", admin.getId())
                .header("Authorization", "Bearer " + jwt(admin))
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());


    }

    @Test
    void shouldReturnAllTasks() throws Exception {
        User admin = createAdmin();
        User targetUser =  createNormalUser();

        Task task = Task.builder()
                .userId(targetUser.getId())
                .title("System Audit")
                .description("Admin checking tasks")
                .priority(Priority.HIGH)
                .estimatedHours(2)
                .deadline(Instant.now().plusSeconds(3600))
                .dependencies(Set.of())
                .status(Status.PENDING)
                .created(Instant.now())
                .build();
        taskRepository.save(task);

        mockMvc.perform(get("/api/admin/users/all-tasks")
        .header("Authorization", "Bearer " + jwt(admin)))
                .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("System Audit"));


    }

    @Test
    void userCannotAccessAdminEndpoints() throws Exception {
        User normalUser = createNormalUser();

        mockMvc.perform(get("/api/admin/users/all-users")
                        .header("Authorization", "Bearer " + jwt(normalUser)))
                .andExpect(status().isForbidden());
    }


    private User createAdmin() {
        User admin = User.builder()
                .username("admin_user")
                .email("admin@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        return userRepository.save(admin);
    }

    private User createNormalUser() {
        User user = User.builder()
                .username("normalUser")
                .email("normalUser" + "@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.USER)
                .enabled(true)
                .build();
       return userRepository.save(user);
    }
}

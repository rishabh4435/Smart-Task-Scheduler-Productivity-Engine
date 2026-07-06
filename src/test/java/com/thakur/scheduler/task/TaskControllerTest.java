package com.thakur.scheduler.task;


import com.thakur.scheduler.config.TestCacheConfig;
import org.springframework.context.annotation.Import;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thakur.scheduler.task.dto.request.DependencyRequestDto;
import com.thakur.scheduler.task.dto.request.StatusUpdateRequest;
import com.thakur.scheduler.task.dto.request.TaskCreateRequestDto;
import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.entity.User;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Role;
import com.thakur.scheduler.task.model.enums.Status;
import com.thakur.scheduler.task.repository.TaskRepository;
import com.thakur.scheduler.task.repository.UserRepository;
import com.thakur.scheduler.task.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestCacheConfig.class)
class TaskControllerTest {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    private String jwt(User user){
        return jwtService.generateToken(user);
    }

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateTaskSuccessfully() throws Exception{
        User testUser = createUser();

        String validJwt = jwt(testUser);

        TaskCreateRequestDto request = new TaskCreateRequestDto(
                "testTask1",
                "this task is for test",
                Priority.LOW,
                6,
                Instant.now().plusSeconds(3600),
                Set.of()
        );

        mockMvc.perform(post("/api/tasks/create-task")
                        .header("Authorization", "Bearer " + validJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("testTask1"))
                .andExpect(jsonPath("$.description").value("this task is for test"))
                .andExpect(jsonPath("$.priority").value("LOW"));


        List<Task> savedTasks = taskRepository.findAll();
        assertEquals(1, savedTasks.size());
        assertEquals("testTask1", savedTasks.getFirst().getTitle());
        assertEquals(testUser.getId(), savedTasks.getFirst().getUserId());
    }

    @Test
    void duplicateTaskCreate_shouldReturnConflict() throws Exception {

        User testUser = createUser();

        String validJwt = jwt(testUser);

        TaskCreateRequestDto request = new TaskCreateRequestDto(
                "testTask1",
                "this task is for test",
                Priority.LOW,
                6,
                Instant.now().plusSeconds(3600),
                Set.of()
        );

        mockMvc.perform(post("/api/tasks/create-task")
                        .header("Authorization", "Bearer " + validJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/tasks/create-task")
                        .header("Authorization", "Bearer " + validJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Task with this title already exists."));
    }

    @Test
    void updateTask_Successful() throws Exception{
        User testUser = createUser();
        String validJwt = jwt(testUser);

        StatusUpdateRequest request ;
        Task testTask = createTask();
        testTask.setUserId(testUser.getId());
        testTask = taskRepository.save(testTask);

        request =
                new StatusUpdateRequest();
        request.setStatus(Status.IN_PROGRESS);

        mockMvc.perform(
                        patch("/api/tasks/update-status/{taskId}", testTask.getId())
                                .header("Authorization", "Bearer " + validJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));


        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();

        assertEquals(
                Status.IN_PROGRESS,
                updatedTask.getStatus(),
                "The task status should be updated in MongoDB!"
        );
    }

    @Test
    void deleteTask_Successful() throws Exception{
        User testUser = createUser();

        String validJwt = jwt(testUser);
        Task testTask = createTask();
        testTask.setUserId(testUser.getId());
        testTask = taskRepository.save(testTask);

        mockMvc.perform(delete("/api/tasks/{taskId}", testTask.getId())
                        .header("Authorization", "Bearer " + validJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Task deleted = taskRepository.findById(testTask.getId()).orElseThrow();

        assertEquals(Status.DELETED, deleted.getStatus());
        assertNotNull(deleted.getDeletedAt());
    }

    @Test
    void addDependentTask_Successful() throws Exception {

        User testUser = createUser();

        String validJwt = jwt(testUser);


        Task depTask = createTask();
        depTask.setUserId(testUser.getId());
        depTask = taskRepository.save(depTask);


        Task testTask = createTask();
        testTask.setUserId(testUser.getId());
        testTask = taskRepository.save(testTask);


        com.thakur.scheduler.task.dto.request.DependencyRequestDto request =
                new DependencyRequestDto();
        request.setDependencyId(depTask.getId());


        mockMvc.perform(patch("/api/tasks/{taskId}/dependencies", testTask.getId())
                        .header("Authorization", "Bearer " + validJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dependencies[0]").value(depTask.getId()));


        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();

        assertTrue(
                updatedTask.getDependencies().contains(depTask.getId()),
                "The main task's dependencies set should now contain the dependent task's ID!"
        );
    }

    @Test
    void removeDependentTask_Successful() throws Exception {

        User testUser = createUser();
        String validJwt = jwt(testUser);


        Task depTask = createTask();
        depTask.setUserId(testUser.getId());
        depTask = taskRepository.save(depTask);


        Task testTask = createTask();
        testTask.setUserId(testUser.getId());


        testTask.setDependencies(new HashSet<>(Set.of(depTask.getId())));
        testTask = taskRepository.save(testTask);

        // 🚀 STEP 4: Perform the DELETE request
        mockMvc.perform(delete(
                        "/api/tasks/{taskId}/dependencies/{dependencyTaskId}",
                        testTask.getId(),
                        depTask.getId())
                        .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dependencies").isEmpty());


        Task updated = taskRepository.findById(testTask.getId()).orElseThrow();

        org.junit.jupiter.api.Assertions.assertFalse(
                updated.getDependencies().contains(depTask.getId()),
                "Dependency should be completely removed from the database!"
        );
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

    private Task createTask() {
        return Task.builder()
                .title("testTask")
                .description("This is test task")
                .priority(Priority.LOW)
                .estimatedHours(6)
                .deadline(Instant.now().plusSeconds(3600))
                .dependencies(Set.of())
                .status(Status.PENDING)
                .created(Instant.now())
                .build();
    }

}



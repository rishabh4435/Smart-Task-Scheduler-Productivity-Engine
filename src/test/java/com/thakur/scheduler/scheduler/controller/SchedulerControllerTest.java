package com.thakur.scheduler.scheduler.controller;


import com.thakur.scheduler.config.TestCacheConfig;
import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.entity.User;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Role;
import com.thakur.scheduler.task.model.enums.Status;
import com.thakur.scheduler.task.repository.TaskRepository;
import com.thakur.scheduler.task.repository.UserRepository;
import com.thakur.scheduler.task.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc()
@ActiveProfiles("test")
@Import(TestCacheConfig.class)
class SchedulerControllerTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;
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
    void testRecommendNextTask_Successfully() throws Exception {
        User testUser = createUser();
        Task task = createTask();
        task.setUserId(testUser.getId());
        taskRepository.save(task);
        String validJwt = jwt(testUser);

        mockMvc.perform(get("/api/scheduler/recommend")
                        .header("Authorization", "Bearer " + validJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(task.getId()))
                .andExpect(jsonPath("$.title").value("testTask"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.recommendationReason").isNotEmpty())
                .andExpect(jsonPath("$.recommendationReason").exists());
    }

    @Test
    void testExecutionOrder_Successfully() throws Exception {


        User testUser = createUser();
        String validJwt = jwt(testUser);


        Task task1 = createTask();
        task1.setUserId(testUser.getId());
        task1.setTitle("Task One");
        taskRepository.save(task1);

        Task task2 = createTask();
        task2.setUserId(testUser.getId());
        task2.setTitle("Task Two");
        taskRepository.save(task2);


        mockMvc.perform(get("/api/scheduler/execution-order")
                        .header("Authorization", "Bearer " + validJwt)
                        .contentType(MediaType.APPLICATION_JSON))


                .andExpect(status().isOk())


                .andExpect(jsonPath("$.size()").value(2))

                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].title").exists())

                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$[1].title").exists());
    }


    @Test
    void testDailyPlan_Successfully() throws Exception {
        User testUser = createUser();
        String validJwt = jwt(testUser);
        Task task1 = createTask();
        task1.setUserId(testUser.getId());
        task1.setTitle("Task One");
        taskRepository.save(task1);
        Task task2 = createTask();
        task2.setUserId(testUser.getId());
        task2.setTitle("Task Two");
        taskRepository.save(task2);

        mockMvc.perform(get("/api/scheduler/daily-plan")
                .param("availableHours", "8")
                .header("Authorization", "Bearer " + validJwt)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks.length()").value(1))
                .andExpect(jsonPath("$.tasks[0].id").exists())
                .andExpect(jsonPath("$.tasks[0].priority").value("LOW"))
                .andExpect(jsonPath("$.tasks[0].status").value("PENDING"))
                .andExpect(jsonPath("$.availableHours").value(8))
                .andExpect(jsonPath("$.totalEstimatedHours").value(6));



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

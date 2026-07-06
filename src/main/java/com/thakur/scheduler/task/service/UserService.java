package com.thakur.scheduler.task.service;

import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.dto.response.UserResponseDto;
import com.thakur.scheduler.task.exception.ResourceNotFoundException;
import com.thakur.scheduler.task.mapper.TaskMapper;
import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.entity.User;
import com.thakur.scheduler.task.model.enums.Role;
import com.thakur.scheduler.task.repository.TaskRepository;
import com.thakur.scheduler.task.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public UserResponseDto updateUserRole(String userId, Role newRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if(user.getRole().equals(newRole)) {
            log.info("User {} already has role {}", userId, newRole);
            return mapToDto(user);
        }

        user.setRole(newRole);
        User saved = userRepository.save(user);
        log.info("User {} role updated to {} by admin", userId, newRole);
        return mapToDto(saved);
    }

    private UserResponseDto mapToDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.isEnabled()
        );
    }

    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public UserResponseDto updateUserStatus(String userId, boolean enabled) {
        log.info("Admin updating user {} status to: {}", userId, enabled);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if(user.isEnabled() == enabled) {
            log.info("User {} is already has status: {}", userId, enabled);
            return mapToDto(user);
        }

        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        log.info("Successfully {} user {}", enabled ? "enabled" : "disabled", userId);
        return mapToDto(saved);

    }

    public List<TaskResponseDto> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(TaskMapper::toResponseDto)
                .toList();
    }

}

package com.thakur.scheduler.task.dto.response;

import com.thakur.scheduler.task.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto{
        String id;
        String username;
        String email;
        Role role;
        Instant createdAt;
        Boolean isEnabled;


}

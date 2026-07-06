package com.thakur.scheduler.task.controller;

import com.thakur.scheduler.task.dto.request.RoleUpdateRequest;
import com.thakur.scheduler.task.dto.request.UserStatusUpdateRequest;
import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.dto.response.UserResponseDto;
import com.thakur.scheduler.task.exception.BadRequestException;
import com.thakur.scheduler.task.security.CustomUserDetails;
import com.thakur.scheduler.task.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController{
    private final UserService userService;


    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponseDto> updateRole(@PathVariable String userId,
                                                      @Valid @RequestBody RoleUpdateRequest request,
                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String adminId = customUserDetails.getUser().getId();
        if(adminId.equals(userId)){
            throw new BadRequestException("Admins cannot change their own role");
        }
        UserResponseDto responseDto = userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponseDto> updateUserStatus(@PathVariable String userId,
                                                            @Valid @RequestBody UserStatusUpdateRequest request,
                                                            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String adminId = customUserDetails.getUser().getId();
        if(adminId.equals(userId) && !request.getEnabled()){
            throw new BadRequestException("Admins cannot change their own role");
        }

        UserResponseDto responseDto = userService.updateUserStatus(userId, request.getEnabled());
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/all-tasks")
    public ResponseEntity<List<TaskResponseDto>> getAllTasks(){
        return new ResponseEntity<>(userService.getAllTasks(), HttpStatus.OK);
    }


}

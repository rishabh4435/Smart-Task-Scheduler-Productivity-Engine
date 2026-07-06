package com.thakur.scheduler.task.controller;

import com.thakur.scheduler.task.dto.request.RoleUpdateRequest;
import com.thakur.scheduler.task.dto.request.UserStatusUpdateRequest;
import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.dto.response.UserResponseDto;
import com.thakur.scheduler.task.exception.BadRequestException;
import com.thakur.scheduler.task.security.CustomUserDetails;
import com.thakur.scheduler.task.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "4. Admin Console", description = "Restricted endpoints for system administration, user roles, and global oversight")
public class AdminController{
    private final UserService userService;

    @Operation(
            summary = "Update user role",
            description = "Promotes or demotes a user's role (e.g., USER to ADMIN). An admin cannot change their own role."
    )
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponseDto> updateRole(
            @Parameter(description = "The unique ID of the target user") @PathVariable String userId,
            @Valid @RequestBody RoleUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String adminId = customUserDetails.getUser().getId();
        if(adminId.equals(userId)){
            throw new BadRequestException("Admins cannot change their own role");
        }
        UserResponseDto responseDto = userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Fetch all system users",
            description = "Retrieves a complete list of all registered users. STRICTLY requires ADMIN role privileges."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user list"),
            @ApiResponse(responseCode = "403", description = "Forbidden (User is not an Admin)")
    })
    @GetMapping("/all-users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @Operation(
            summary = "Enable or disable a user account",
            description = "Updates the active status of a user. Disabled users cannot log in. Admins cannot disable themselves."
    )
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponseDto> updateUserStatus(
            @Parameter(description = "The unique ID of the target user") @PathVariable String userId,
            @Valid @RequestBody UserStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String adminId = customUserDetails.getUser().getId();
        if(adminId.equals(userId) && !request.getEnabled()){
            throw new BadRequestException("Admins cannot change their own status"); // Fixed text slightly for clarity
        }

        UserResponseDto responseDto = userService.updateUserStatus(userId, request.getEnabled());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Fetch all system tasks",
            description = "Retrieves a complete list of all tasks created by any user across the system. STRICTLY requires ADMIN role privileges."
    )
    @GetMapping("/all-tasks")
    public ResponseEntity<List<TaskResponseDto>> getAllTasks(){
        return new ResponseEntity<>(userService.getAllTasks(), HttpStatus.OK);
    }
}
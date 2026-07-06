package com.thakur.scheduler.scheduler.controller;

import com.thakur.scheduler.scheduler.dto.DailyPlanResponseDto;
import com.thakur.scheduler.scheduler.dto.RecommendationResponseDto;
import com.thakur.scheduler.scheduler.service.SchedulerService;
import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.security.CustomUserDetails;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scheduler")
@Validated
public class SchedulerController {

    private final SchedulerService schedulerService;



    @GetMapping("/recommend")
    public ResponseEntity<RecommendationResponseDto> recommend(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                schedulerService.recommendNextTask(
                        userDetails.getUser().getId()));
    }

    @GetMapping("/execution-order")
    public ResponseEntity<List<TaskResponseDto>> executionOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                schedulerService.getExecutionOrder(
                        userDetails.getUser().getId()));
    }

    @GetMapping("/daily-plan")
    public ResponseEntity<DailyPlanResponseDto> dailyPlan(
            @RequestParam @Min(1) @Max(24) int availableHours,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                schedulerService.getDailyPlan(
                        userDetails.getUser().getId(),
                        availableHours));
    }


}
package com.thakur.scheduler.scheduler.service;

import com.thakur.scheduler.core.engine.SchedulerEngine;
import com.thakur.scheduler.core.mapper.EngineTaskMapper;
import com.thakur.scheduler.core.model.EngineTask;
import com.thakur.scheduler.scheduler.dto.DailyPlanResponseDto;
import com.thakur.scheduler.scheduler.dto.Recommendation;
import com.thakur.scheduler.scheduler.dto.RecommendationResponseDto;
import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.exception.ResourceNotFoundException;
import com.thakur.scheduler.task.mapper.TaskMapper;
import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.enums.Status;
import com.thakur.scheduler.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class  SchedulerService {

    private final SchedulerEngine schedulerEngine;
    private final TaskRepository taskRepository;


    @Cacheable(value = "recommendations", key = "#userId")
    public RecommendationResponseDto recommendNextTask(String userId) {

        SchedulerContext context = buildSchedulerContext(userId);

        Recommendation recommendation = schedulerEngine.recommendNextTask(
                        context.engineTasks(),
                        context.unavailableTaskIds()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("No ready task found for user: " + userId)
                );

        Task task = getTaskOrThrow(context, recommendation.task());

        RecommendationResponseDto response = new RecommendationResponseDto(
                task.getId(),
                task.getTitle(),
                task.getPriority(),
                task.getStatus(),
                task.getDeadline(),
                task.getEstimatedHours(),
                recommendation.reason()
        );

        log.info("Returning response: {}", response);
        return response;
    }
    @Cacheable(
            value = "executionOrder",
            key = "#userId"
    )
    public List<TaskResponseDto> getExecutionOrder(String userId){
        SchedulerContext context = buildSchedulerContext(userId);

        List<EngineTask> executionOrder =
                schedulerEngine.executionSequence(context.engineTasks());

        return executionOrder.stream()
                .map(engineTask -> {
                    Task task = getTaskOrThrow(context, engineTask);
                    return TaskMapper.toResponseDto(task);
                })
                .toList();
    }


    private SchedulerContext buildSchedulerContext(String userId){

        List<Task> tasks= taskRepository.findByUserIdAndDeletedAtIsNull(userId);

        if (tasks.isEmpty()) {
            throw new ResourceNotFoundException("No active tasks found for user: " + userId);
        }

        Map<String, Task> taskLookup = tasks.stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));


        List<EngineTask> engineTasks = tasks.stream()
                .filter(task ->
                        task.getStatus() != Status.CANCELED
                )
                .map(EngineTaskMapper::toEngineTask)
                .toList();
        Set<String> unavailableTaskIds = tasks.stream()
                .filter(task -> task.getStatus() == Status.COMPLETED || task.getStatus() == Status.CANCELED)
                .map(Task::getId)
                .collect(Collectors.toSet());

        return new SchedulerContext(
                tasks,
                engineTasks,
                taskLookup,
                unavailableTaskIds
        );

    }

    @Cacheable(
            value = "dailyPlan",
            key = "#userId + '_' + #availableHours"
    )
    public DailyPlanResponseDto getDailyPlan(String userId, int availableHours){
        SchedulerContext context = buildSchedulerContext(userId);

        List<EngineTask> dailyPlan =
                schedulerEngine.getDailyPlan(
                        context.engineTasks(),
                        context.unavailableTaskIds(),
                        availableHours
                );

        List<TaskResponseDto> taskDtos = dailyPlan.stream()
                .map(engineTask -> {
                    Task task = getTaskOrThrow(context, engineTask);

                    return TaskMapper.toResponseDto(task);
                })
                .toList();

        int usedHours = dailyPlan.stream()
                .mapToInt(EngineTask::estimateHours)
                .sum();
        return new DailyPlanResponseDto(
                taskDtos,
                availableHours,
                usedHours
        );
    }

    private Task getTaskOrThrow(SchedulerContext context, EngineTask engineTask) {

        return Optional.ofNullable(
                context.taskLookup().get(engineTask.id())
        ).orElseThrow(() ->
                new IllegalStateException(
                        "Engine returned unknown task: " + engineTask.id()
                )
        );
    }

    private record SchedulerContext(
            List<Task> tasks,
            List<EngineTask> engineTasks,
            Map<String, Task> taskLookup,
            Set<String> unavailableTaskIds
    ) {
    }


}

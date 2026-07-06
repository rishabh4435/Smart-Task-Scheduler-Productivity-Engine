package com.thakur.scheduler.task.mapper;

import com.thakur.scheduler.task.dto.response.TaskResponseDto;
import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {

    @Test
    void testToResponseDto_Success() {
        Instant deadline = Instant.now();

        Task task = new Task();
        task.setId("Task123");
        task.setTitle("Design DB");
        task.setDescription("Create MongoDB schemas");
        task.setPriority(Priority.CRITICAL);
        task.setStatus(Status.IN_PROGRESS);
        task.setDeadline(deadline);
        task.setDependencies(Set.of("Task001"));

        TaskResponseDto dto = TaskMapper.toResponseDto(task);

        assertNotNull(dto);
        assertEquals("Task123", dto.getId());
        assertEquals("Design DB", dto.getTitle());
        assertEquals("Create MongoDB schemas", dto.getDescription());
        assertEquals(Priority.CRITICAL, dto.getPriority());
        assertEquals(Status.IN_PROGRESS, dto.getStatus());
        assertEquals(deadline, dto.getDeadline());
        assertEquals(1, dto.getDependencies().size());
        assertTrue(dto.getDependencies().contains("Task001"));
    }
}
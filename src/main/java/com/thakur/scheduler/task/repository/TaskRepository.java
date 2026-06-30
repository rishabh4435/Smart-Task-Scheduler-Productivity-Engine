package com.thakur.scheduler.task.repository;

import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByUserId(String userId);
    Optional<Task> findByUserIdAndDeletedAtIsNull(String taskId, String userId);
    Optional<Task> findByIdAndUserId(String id, String userId);
    List<Task> findByUserIdAndDependenciesContaining(String userId, String taskId);
    List<Task> findByUserIdAndStatus(String userId, Status status);
    List<Task> findAllByUserIdAndIsDeletedFalse(String userId);
    List<Task> findByUserIdAndPriority(String userId, Priority priority);
}

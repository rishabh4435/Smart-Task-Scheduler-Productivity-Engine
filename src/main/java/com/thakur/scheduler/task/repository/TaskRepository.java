package com.thakur.scheduler.task.repository;

import com.thakur.scheduler.task.model.entity.Task;
import com.thakur.scheduler.task.model.enums.Priority;
import com.thakur.scheduler.task.model.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;



import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    Page<Task> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);
    List<Task> findByUserIdAndDeletedAtIsNull(String userId);
    long countByIdInAndUserIdAndDeletedAtIsNull(Set<String> id, String userId);
    Optional<Task> findByIdAndUserIdAndDeletedAtIsNull(String id, String userId);
    List<Task> findByUserIdAndDependenciesContaining(String userId, String dependencyId);
    List<Task> findByUserIdAndDeletedAtIsNullAndStatus(String userId, Status status);
    List<Task> findByUserIdAndDeletedAtIsNullAndPriority(String userId, Priority priority);
    boolean existsByUserIdAndTitleAndDeletedAtIsNull(String userId, String title);


}

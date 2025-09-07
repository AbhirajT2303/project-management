package com.project_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project_management.entities.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByTenant_Id(UUID tenantId);

    Optional<Task> findByIdAndTenant_Id(Long id, UUID tenantId);

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.id = :taskId AND t.tenant.id = :tenantId")
    boolean existsByIdAndTenantId(@Param("taskId") Long taskId, @Param("tenantId") UUID tenantId);
}

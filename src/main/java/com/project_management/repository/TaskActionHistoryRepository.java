package com.project_management.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project_management.entities.Task;
import com.project_management.entities.TaskActionHistory;

@Repository
public interface TaskActionHistoryRepository extends JpaRepository<TaskActionHistory, Long> {

    @Query("SELECT tah FROM TaskActionHistory tah WHERE tah.task = :task AND tah.tenant.id = :tenantId ORDER BY tah.performedAt DESC")
    List<TaskActionHistory> findByTaskAndTenantIdOrderByPerformedAtDesc(@Param("task") Task task, @Param("tenantId") UUID tenantId);

    @Query("SELECT tah FROM TaskActionHistory tah WHERE tah.task.id = :taskId AND tah.tenant.id = :tenantId ORDER BY tah.performedAt DESC")
    List<TaskActionHistory> findByTaskIdAndTenantIdOrderByPerformedAtDesc(@Param("taskId") Long taskId, @Param("tenantId") UUID tenantId);

    @Query("SELECT tah FROM TaskActionHistory tah WHERE tah.performedBy = :performedBy AND tah.tenant.id = :tenantId")
    List<TaskActionHistory> findByPerformedByAndTenantId(@Param("performedBy") String performedBy, @Param("tenantId") UUID tenantId);
}

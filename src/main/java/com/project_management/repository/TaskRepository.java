package com.project_management.repository;

import com.project_management.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findAllByTenant_Id(UUID tenantId);

    Optional<Task> findByIdAndTenant_Id(Long id, UUID tenantId);

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.id = :taskId AND t.tenant.id = :tenantId")
    boolean existsByIdAndTenantId(@Param("taskId") Long taskId, @Param("tenantId") UUID tenantId);

    Page<Task> findAllByTenant_Id(UUID tenantId, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.tenant.id = :tenantId")
    long countByTenantId(@Param("tenantId") UUID tenantId);
}

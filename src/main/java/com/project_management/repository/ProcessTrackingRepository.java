package com.project_management.repository;

import com.project_management.entities.ProcessStatus;
import com.project_management.entities.ProcessTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessTrackingRepository extends JpaRepository<ProcessTracking, UUID> {
    Optional<ProcessTracking> findByProcessIdAndTenantId(UUID processId, UUID tenantId);

    List<ProcessTracking> findByTenantIdOrderByStartedAtDesc(UUID tenantId);

    List<ProcessTracking> findByTenantIdAndStatusOrderByStartedAtDesc(UUID tenantId, ProcessStatus status);

}

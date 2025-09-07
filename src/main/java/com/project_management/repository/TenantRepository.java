package com.project_management.repository;

import com.project_management.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByIdAndIsActiveTrue(UUID id);

    Optional<Tenant> findByTenantName(String name);

    List<Tenant> findAllByIsActiveTrue();

    boolean existsByIdAndIsActiveTrue(UUID id);
}

package com.project_management.service;

import com.project_management.entities.Tenant;

import java.util.List;
import java.util.UUID;

public interface TenantService {
    Tenant createTenant(Tenant tenant);

    Tenant updateTenant(UUID id, Tenant tenant);

    void deleteTenant(UUID id);

    Tenant getTenantById(UUID id);

    List<Tenant> getAllTenant();

    boolean existsById(UUID id);
}

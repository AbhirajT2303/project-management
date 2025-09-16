package com.project_management.service;

import com.project_management.dto.TenantDto;

import java.util.List;
import java.util.UUID;

public interface TenantService {
    TenantDto createTenant(TenantDto tenant);

    TenantDto updateTenant(UUID id, TenantDto tenant);

    void deleteTenant(UUID id);

    TenantDto getTenantById(UUID id);

    List<TenantDto> getAllTenant();

    boolean existsById(UUID id);
}

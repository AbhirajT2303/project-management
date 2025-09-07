package com.project_management.service;

import com.project_management.entities.Tenant;
import com.project_management.exception.ResourceNotFoundException;
import com.project_management.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public Tenant createTenant(Tenant tenant) {
        tenant.setId(null);
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }

    @Override
    public Tenant updateTenant(UUID id, Tenant tenant) {
        Tenant existing = tenantRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Tenant", "id", id));
        existing.setTenantName(tenant.getTenantName());
        existing.setActive(tenant.isActive());
        return tenantRepository.save(existing);
    }

    @Override
    public void deleteTenant(UUID id) {
        Tenant existing = tenantRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Tenant", "id", id));
        tenantRepository.delete(existing);
    }

    @Override
    public Tenant getTenantById(UUID id) {
        return tenantRepository.findByIdAndIsActiveTrue(id).orElseThrow(
                () -> new ResourceNotFoundException("Tenant", "id", id)
        );
    }

    @Override
    public List<Tenant> getAllTenant() {
        return tenantRepository.findAllByIsActiveTrue();
    }

    @Override
    public boolean existsById(UUID id) {
        log.info("in exist by id in tenant serviceL: "+ id);
        return tenantRepository.existsByIdAndIsActiveTrue(id);
    }
}

package com.project_management.service;

import com.project_management.dto.TenantDto;
import com.project_management.entities.Tenant;
import com.project_management.exception.ResourceNotFoundException;
import com.project_management.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    @Override
    public TenantDto createTenant(TenantDto tenantDto) {
        Tenant tenant = modelMapper.map(tenantDto, Tenant.class);
        tenant.setId(null);
        tenant.setActive(true);

        return modelMapper.map(tenantRepository.save(tenant), TenantDto.class);
    }

    @Override
    public TenantDto updateTenant(UUID id, TenantDto tenant) {
        Tenant existing = tenantRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Tenant", "id", id));
        existing.setTenantName(tenant.getTenantName());
        existing.setActive(tenant.isActive());
        return modelMapper.map(tenantRepository.save(existing), TenantDto.class);
    }

    @Override
    public void deleteTenant(UUID id) {
        Tenant existing = tenantRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Tenant", "id", id));
        tenantRepository.delete(existing);
    }

    @Override
    public TenantDto getTenantById(UUID id) {
        return modelMapper.map(tenantRepository.findByIdAndIsActiveTrue(id).orElseThrow(
                () -> new ResourceNotFoundException("Tenant", "id", id)
        ), TenantDto.class);
    }

    @Override
    public List<TenantDto> getAllTenant() {
        return tenantRepository.findAllByIsActiveTrue().stream().map(tenant -> modelMapper.map(tenant, TenantDto.class)).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        log.info("in exist by id in tenant serviceL: " + id);
        return tenantRepository.existsByIdAndIsActiveTrue(id);
    }
}

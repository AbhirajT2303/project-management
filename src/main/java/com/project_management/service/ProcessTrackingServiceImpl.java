package com.project_management.service;

import com.project_management.context.TenantContext;
import com.project_management.controller.TenantController;
import com.project_management.entities.ProcessTracking;
import com.project_management.exception.ResourceNotFoundException;
import com.project_management.repository.ProcessTrackingRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ProcessTrackingServiceImpl implements ProcessTrackingService {
    private final ProcessTrackingRepository processTrackingRepository;

    @Override
    public ProcessTracking getProcessById(UUID processId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        log.info("tenant id in the process tracking service: {}",tenantId);
        return processTrackingRepository.findByProcessIdAndTenantId(processId, tenantId).orElseThrow(() -> new ResourceNotFoundException("Process not found with process Id: " + processId));
    }

    @Override
    public List<ProcessTracking> getAllProcesses() {
        return processTrackingRepository.findByTenantIdOrderByStartedAtDesc(TenantContext.getCurrentTenant());
    }
}

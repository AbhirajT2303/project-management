package com.project_management.service;

import com.project_management.entities.ProcessTracking;

import java.util.List;
import java.util.UUID;

public interface ProcessTrackingService {
    ProcessTracking getProcessById(UUID processId);
    List<ProcessTracking> getAllProcesses();
}

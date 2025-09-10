package com.project_management.controller;

import com.project_management.dto.ApiResponse;
import com.project_management.entities.ProcessTracking;
import com.project_management.service.ProcessTrackingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@Validated
@RequestMapping("api/v1/process")
@AllArgsConstructor
public class ProcessTrackingController {

    private final ProcessTrackingService processTrackingService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessTracking>> getProcessTrackingById(@Valid @PathVariable String id) {
        UUID processId = UUID.fromString(id);
        ProcessTracking process = processTrackingService.getProcessById(processId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Process Tracking fetched successfully", process));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProcessTracking>>> getAllProcessTracking() {
        List<ProcessTracking> processes = processTrackingService.getAllProcesses();
        return ResponseEntity.ok(new ApiResponse<>(true, "", processes));
    }
}

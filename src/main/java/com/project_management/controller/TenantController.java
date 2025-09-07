package com.project_management.controller;

import com.project_management.dto.ApiResponse;
import com.project_management.entities.Tenant;
import com.project_management.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@Validated
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<ApiResponse<Tenant>> createTenant(@Valid @RequestBody Tenant tenant) {
        Tenant created = tenantService.createTenant(tenant);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Tenant created successful", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Tenant>> getTenant(@PathVariable UUID id) {
        Tenant tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tenant retrieved successfully", tenant));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Tenant>>> getAllTenant() {
        List<Tenant> tenants = tenantService.getAllTenant();
        return ResponseEntity.ok(new ApiResponse<>(true, "Tenants retrieved successfully", tenants));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Tenant>> updateTenant(@PathVariable UUID id, @Valid @RequestBody Tenant tenant) {
        Tenant updatedTenant = tenantService.updateTenant(id, tenant);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tenant updated successfully", updatedTenant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(@PathVariable UUID id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tenant deleted successfully", null));
    }
}

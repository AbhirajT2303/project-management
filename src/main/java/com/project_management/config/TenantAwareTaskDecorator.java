package com.project_management.config;

import com.project_management.context.TenantContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantAwareTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return () -> {
            try {
                TenantContext.setCurrentTenant(tenantId);
                runnable.run();
            } finally {
                TenantContext.clear();
            }
        };
    }
}

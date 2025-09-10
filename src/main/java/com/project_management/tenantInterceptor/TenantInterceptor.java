package com.project_management.tenantInterceptor;

import com.project_management.context.TenantContext;
import com.project_management.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private final TenantService tenantService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        log.info("Tenant Interceptor - Processing request to URI: {}", request.getRequestURI());

        String tenantIdString = null;
        String source = null;

        tenantIdString = request.getHeader(TENANT_HEADER);

        if (tenantIdString != null && !tenantIdString.trim().isEmpty()) {
            source = "header";
            log.info("Found tenantId in {}: {}", source, tenantIdString);
        } else {
            log.debug("No tenant ID found in header, checking if request is multipart...");

            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                tenantIdString = multipartRequest.getParameter("X-Tenant-ID");

                if (tenantIdString != null && !tenantIdString.trim().isEmpty()) {
                    source = "multipart form-data";
                    log.info("Found tenantId in {}: {}", source, tenantIdString);
                } else {
                    log.error("Tenant ID missing in multipart form-data for URI: {}", request.getRequestURI());
                    sendErrorResponse(response, "Tenant ID is required in X-Tenant-ID header or tenantId form field");
                    return false;
                }
            } else {
                log.error("Tenant ID missing in header for non-multipart request to URI: {}", request.getRequestURI());
                sendErrorResponse(response, "Tenant ID is required in X-Tenant-ID header");
                return false;
            }
        }

        try {
            UUID tenantId = UUID.fromString(tenantIdString.trim());
            log.info("Parsed tenant ID: {} from {}", tenantId, source);

            if (!tenantService.existsById(tenantId)) {
                log.error("Tenant with ID {} not found or inactive", tenantId);
                sendErrorResponse(response, "Tenant not found or inactive");
                return false;
            }

            TenantContext.setCurrentTenant(tenantId);
            log.info("Tenant context successfully set for tenant ID: {} from {}", tenantId, source);
            return true;

        } catch (IllegalArgumentException e) {
            log.error("Invalid tenant ID format: '{}' from {} - Error: {}",
                    tenantIdString, source, e.getMessage());
            sendErrorResponse(response, "Invalid tenant ID format. Expected UUID format.");
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while processing tenant ID: '{}' from {} - Error: {}",
                    tenantIdString, source, e.getMessage(), e);
            sendErrorResponse(response, "Error processing tenant information");
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format("{\"success\":false,\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message, java.time.Instant.now().toString());

        response.getWriter().write(jsonResponse);
        log.warn("Sent error response: {}", message);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        try {
            UUID tenantId = TenantContext.getCurrentTenant();
            TenantContext.clear();
        } catch (Exception e) {
            TenantContext.clear();
        }
    }
}

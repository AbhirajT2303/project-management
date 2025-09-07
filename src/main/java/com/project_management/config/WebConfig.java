package com.project_management.config;

import com.project_management.tenantInterceptor.TenantInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {
    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        log.info("in config -- "+tenantInterceptor);
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/v1/tasks/**")
                .excludePathPatterns("/api/v1/tenants/**");
    }
}

package com.project_management.service;

import com.project_management.dto.TaskRequestDto;
import com.project_management.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {
    private final StatelessKieSession statelessKieSession;

    public ValidationResult validateTaskRequest(TaskRequestDto dto) {
        log.debug("Validating TaskRequestDto using Drools rules");

        ValidationResult result = new ValidationResult();
        statelessKieSession.execute(Arrays.asList(dto, result));

        log.debug("Validation completed. Found {} violations", result.getViolations().size());
        return result;
    }
}

package com.project_management.service;

import com.project_management.dto.TaskActionRequestDto;
import com.project_management.dto.TaskResponseDto;

public interface TaskActionService {
	TaskResponseDto executeAction(Long taskId, TaskActionRequestDto request);
}

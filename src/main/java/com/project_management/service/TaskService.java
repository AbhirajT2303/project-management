package com.project_management.service;

import java.util.List;

import com.project_management.dto.TaskRequestDto;
import com.project_management.dto.TaskResponseDto;

public interface TaskService {
	TaskResponseDto createTask(TaskRequestDto  taskRequestDto);

	TaskResponseDto updateTask(Long taskId, TaskRequestDto taskRequestDto);

	TaskResponseDto getTaskById(Long taskId);

	List<TaskResponseDto> getAllTasks();

	boolean deleteTask(Long taskId);
}

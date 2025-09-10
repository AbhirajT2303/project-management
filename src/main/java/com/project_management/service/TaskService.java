package com.project_management.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project_management.dto.ImportResult;
import com.project_management.dto.TaskRequestDto;
import com.project_management.dto.TaskResponseDto;

public interface TaskService {
	TaskResponseDto createTask(TaskRequestDto  taskRequestDto);

	TaskResponseDto updateTask(Long taskId, TaskRequestDto taskRequestDto);

	TaskResponseDto getTaskById(Long taskId);

	List<TaskResponseDto> getAllTasks();

	boolean deleteTask(Long taskId);

	void importCsvAsync(MultipartFile file, String processId);

	void importExcelAsync(MultipartFile file, String processId);
}

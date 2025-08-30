package com.project_management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project_management.dto.ApiResponse;
import com.project_management.dto.TaskRequestDto;
import com.project_management.dto.TaskResponseDto;
import com.project_management.service.TaskService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/tasks")
@Validated
@Slf4j
@RequiredArgsConstructor
public class TaskController {

	private final TaskService taskService;

	@PostMapping
	public ResponseEntity<ApiResponse<TaskResponseDto>> createTask(@Valid @RequestBody TaskRequestDto taskRequest) {
		log.info("Creating new task: {}", taskRequest.getTaskName());
		TaskResponseDto savedTask = taskService.createTask(taskRequest);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse<>(true, "Task Created successfully", savedTask));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TaskResponseDto>> getTask(@PathVariable @Positive Long id) {
		TaskResponseDto task = taskService.getTaskById(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "Task fetched successfully", task));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getAllTasks() {
		List<TaskResponseDto> tasks = taskService.getAllTasks();
		return ResponseEntity.ok(new ApiResponse<>(true, "Task fetched successfully", tasks));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<TaskResponseDto>> updateTask(@PathVariable @Positive Long id,
			@Valid @RequestBody TaskRequestDto taskRequest) {

		TaskResponseDto updatedTask = taskService.updateTask(id, taskRequest);
		return ResponseEntity.ok(new ApiResponse<>(true, "Task updated successfully", updatedTask));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable @Positive Long id) {
		taskService.deleteTask(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "Task deleted successfully", null));
	}
}

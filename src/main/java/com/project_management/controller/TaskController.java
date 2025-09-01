package com.project_management.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project_management.dto.ApiResponse;
import com.project_management.dto.ImportResult;
import com.project_management.dto.TaskActionRequestDto;
import com.project_management.dto.TaskRequestDto;
import com.project_management.dto.TaskResponseDto;
import com.project_management.service.TaskActionService;
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
	private final TaskActionService taskActionService;

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

	@PostMapping(value = "/upload", consumes = "multipart/form-data")
	public ResponseEntity<ApiResponse<Map<String, Object>>> uploadTasks(@RequestPart("file") MultipartFile file) {

		// Validate file
		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, "File is empty", null));
		}

		String filename = file.getOriginalFilename();
		if (filename == null) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid file name", null));
		}

		String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

		try {
			ImportResult result;

			if ("csv".equals(extension)) {
				log.info("Processing CSV file: {}", filename);
				result = taskService.importCsv(file);
			} else if ("xls".equals(extension) || "xlsx".equals(extension)) {
				log.info("Processing Excel file: {}", filename);
				result = taskService.importExcel(file);
			} else {
				return ResponseEntity.badRequest().body(new ApiResponse<>(false,
						"Unsupported file type. Only CSV, XLS, and XLSX files are allowed", null));
			}

			Map<String, Object> responseData = new HashMap<>();
			responseData.put("fileName", result.getFileName());
			responseData.put("totalRowsFound", result.getTotalRows());
			responseData.put("totalRowsSaved", result.getRowsSaved());
			responseData.put("totalRowsSkipped", result.getRowsSkipped());
			responseData.put("rowsWithData", result.getRowsWithData());
			responseData.put("detectedColumnMappings", result.getDetectedColumnMappings());
			responseData.put("aiMappingUsed", result.isAiMappingUsed());

			String message = String.format("Import completed: %d total rows, %d saved, %d skipped from %s",
					result.getTotalRows(), result.getRowsSaved(), result.getRowsSkipped(), filename);

			log.info("Successfully completed import: {}", message);
			return ResponseEntity.ok(new ApiResponse<>(true, message, responseData));

		} catch (Exception e) {
			log.error("Error importing file {}: {}", filename, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse<>(false, "Import failed: " + e.getMessage(), null));
		}
	}

	@PutMapping("/{id}/actions")
	public ResponseEntity<ApiResponse<TaskResponseDto>> executeAction(@PathVariable @Positive Long id,
			@Valid @RequestBody TaskActionRequestDto actionRequest) {
		try {
			TaskResponseDto updatedTask = taskActionService.executeAction(id, actionRequest);
			return ResponseEntity.ok(new ApiResponse<>(true, "Action executed successfully", updatedTask));
		} catch (Exception e) {
			log.error("Error executing action: {}", e.getMessage());
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
		}
	}
}

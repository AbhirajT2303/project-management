package com.project_management.controller;

import com.project_management.dto.*;
import com.project_management.entities.Task;
import com.project_management.repository.TaskActionHistoryRepository;
import com.project_management.repository.TaskRepository;
import com.project_management.service.TaskActionService;
import com.project_management.service.TaskService;
import com.project_management.service.ValidationService;
import com.project_management.specification.TaskSpecifications;
import com.project_management.util.ByteArrayMultipartFile;
import com.project_management.validation.ValidationResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/tasks")
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

    private final TaskService taskService;
    private final TaskActionService taskActionService;
    private final ValidationService validationService;
    private final TaskRepository taskRepository;
    private final TaskActionHistoryRepository taskActionHistoryRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDto>> createTask(@Valid @RequestBody TaskRequestDto taskRequest) {
        log.info("Creating new task: {}", taskRequest.getTaskName());

        ValidationResult validation = validationService.validateTaskRequest(taskRequest);
        if (validation.hasErrors()) {
            String errorMessage = validation.getViolations().stream()
                    .map(v -> v.getField() + ": " + v.getMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Validation failed");

            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, errorMessage, null));
        }

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

        ValidationResult validation = validationService.validateTaskRequest(taskRequest);
        if (validation.hasErrors()) {
            String errorMessage = validation.getViolations().stream()
                    .map(v -> v.getField() + ": " + v.getMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Validation failed");

            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, errorMessage, null));
        }
        TaskResponseDto updatedTask = taskService.updateTask(id, taskRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Task updated successfully", updatedTask));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable @Positive Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Task deleted successfully", null));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProcessIdResponse>> uploadTasks(@RequestPart("file") MultipartFile file) {


        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "File is empty", null));
        }
        String processId = UUID.randomUUID().toString();
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid file name", null));
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        try {

            byte[] fileBytes = file.getBytes();
            MultipartFile persistentFile = new ByteArrayMultipartFile(file.getName(), filename, file.getContentType(), fileBytes);

            if ("csv".equals(extension)) {
                log.info("Processing CSV file: {}", filename);
                taskService.importCsvAsync(persistentFile, processId);
            } else if ("xls".equals(extension) || "xlsx".equals(extension)) {
                log.info("Processing Excel file: {}", filename);
                taskService.importExcelAsync(persistentFile, processId);
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false,
                        "Unsupported file type. Only CSV, XLS, and XLSX files are allowed", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true,
                    "Data processing has begun in the background. Use the process ID to check status.",
                    new ProcessIdResponse(processId)));
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
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));

        } catch (Exception e) {
            log.error("Error executing action: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TaskResponseDto>>> searchTasks(TaskSearchDto searchDto) {
        log.info("Task search request received with criteria: {}", searchDto);

        Page<TaskResponseDto> tasks = taskService.searchTasks(searchDto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                String.format("Found %d tasks matching search criteria (page %d of %d)",
                        tasks.getTotalElements(), tasks.getNumber() + 1, tasks.getTotalPages()),
                tasks
        ));
    }

    @GetMapping("/search/list")
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> searchTasksList(TaskSearchDto searchDto) {
        log.info("Task search list request received with criteria: {}", searchDto);

        List<TaskResponseDto> tasks = taskService.searchTasksList(searchDto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                String.format("Found %d tasks matching search criteria", tasks.size()),
                tasks
        ));
    }

    @GetMapping("/search/count")
    public ResponseEntity<ApiResponse<Long>> getTaskCount(TaskSearchDto searchDto) {
        log.info("Task count request received with criteria: {}", searchDto);

        Specification<Task> spec = TaskSpecifications.withDynamicSearch(
                searchDto.getTaskName(),
                searchDto.getDescription(),
                searchDto.getStatus(),
                searchDto.getPriority(),
                searchDto.getAssignee(),
                searchDto.getDueDateFrom(),
                searchDto.getDueDateTo()
        );

        long count = taskRepository.count(spec);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                String.format("Total tasks matching criteria: %d", count),
                count
        ));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<TaskActionHistoryDto>>> getTaskHistory(@PathVariable Long id) {
        List<TaskActionHistoryDto> history = taskService.getHistoryById(id);
        if (history.isEmpty())
            return ResponseEntity.ok(new ApiResponse<>(true, "Task with task Id: " + id + " Not available", null));
        return ResponseEntity.ok(new ApiResponse<>(true, "Task history fetched successfully", history));
    }
}

package com.project_management.controller;

import com.project_management.dto.*;
import com.project_management.service.TaskActionService;
import com.project_management.service.TaskService;
import com.project_management.util.ByteArrayMultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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
            MultipartFile persistentFile = new ByteArrayMultipartFile(file.getName(),filename,file.getContentType(),fileBytes);

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
}

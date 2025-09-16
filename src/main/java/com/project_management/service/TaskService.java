package com.project_management.service;

import com.project_management.dto.TaskActionHistoryDto;
import com.project_management.dto.TaskRequestDto;
import com.project_management.dto.TaskResponseDto;
import com.project_management.dto.TaskSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskService {
    TaskResponseDto createTask(TaskRequestDto taskRequestDto);

    TaskResponseDto updateTask(Long taskId, TaskRequestDto taskRequestDto);

    TaskResponseDto getTaskById(Long taskId);

    List<TaskResponseDto> getAllTasks();

    boolean deleteTask(Long taskId);

    void importCsvAsync(MultipartFile file, String processId);

    void importExcelAsync(MultipartFile file, String processId);

    Page<TaskResponseDto> searchTasks(TaskSearchDto searchDto);

    List<TaskResponseDto> searchTasksList(TaskSearchDto searchDto);

    List<TaskActionHistoryDto> getHistoryById(Long id);
}

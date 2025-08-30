package com.project_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.project_management.dto.TaskRequestDto;
import com.project_management.dto.TaskResponseDto;
import com.project_management.entities.Task;
import com.project_management.exception.ResourceNotFoundException;
import com.project_management.repository.TaskRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

	private final TaskRepository taskRepository;
	private final ModelMapper modelMapper;

	@Override
	public TaskResponseDto createTask(TaskRequestDto taskRequestDto) {
		log.info("Creating new task:{}", taskRequestDto.getTaskName());

		Task task = modelMapper.map(taskRequestDto, Task.class);
		Task savedTask = taskRepository.save(task);
		return modelMapper.map(savedTask, TaskResponseDto.class);
	}

	@Override
	public TaskResponseDto updateTask(Long taskId, TaskRequestDto taskRequestDto) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

		modelMapper.map(taskRequestDto, task);

		Task updatedTask = taskRepository.save(task);

		return modelMapper.map(updatedTask, TaskResponseDto.class);
	}

	@Override
	public TaskResponseDto getTaskById(Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

		return modelMapper.map(task, TaskResponseDto.class);
	}

	@Override
	public List<TaskResponseDto> getAllTasks() {

		return taskRepository.findAll().stream().map(task -> modelMapper.map(task, TaskResponseDto.class))
				.collect(Collectors.toList());
	}

	@Override
	public boolean deleteTask(Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
		taskRepository.delete(task);
		return true;
	}
}

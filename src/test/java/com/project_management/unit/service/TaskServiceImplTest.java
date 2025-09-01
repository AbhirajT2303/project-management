package com.project_management.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.project_management.dto.TaskRequestDto;
import com.project_management.dto.TaskResponseDto;
import com.project_management.entities.Priority;
import com.project_management.entities.Status;
import com.project_management.entities.Task;
import com.project_management.exception.ResourceNotFoundException;
import com.project_management.repository.TaskRepository;
import com.project_management.service.TaskServiceImpl;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private ModelMapper modelMapper; // ← Add this mock

	@InjectMocks
	private TaskServiceImpl taskService;

	@Test
	void createTask_ShouldSaveTask_WhenValidInput() {
		// Arrange
		TaskRequestDto dto = new TaskRequestDto();
		dto.setTaskName("Test Task");
		dto.setDescription("Test Description");
//		dto.setStatus(Status.ASSIGNED);
		dto.setPriority(Priority.HIGH);
		dto.setDueDate(LocalDate.now().plusDays(5));

		Task entity = new Task();
		entity.setTaskName("Test Task");
		entity.setDescription("Test Description");
		entity.setStatus(Status.ASSIGNED);
		entity.setPriority(Priority.HIGH);
		entity.setDueDate(LocalDate.now().plusDays(5));

		Task savedEntity = new Task();
		savedEntity.setId(1L);
		savedEntity.setTaskName("Test Task");
		savedEntity.setDescription("Test Description");

		TaskResponseDto responseDto = new TaskResponseDto();
		responseDto.setId(1L);
		responseDto.setTaskName("Test Task");
		responseDto.setDescription("Test Description");

		// Mock ModelMapper behavior
		when(modelMapper.map(dto, Task.class)).thenReturn(entity);
		when(modelMapper.map(savedEntity, TaskResponseDto.class)).thenReturn(responseDto);
		when(taskRepository.save(entity)).thenReturn(savedEntity);

		// Act
		TaskResponseDto result = taskService.createTask(dto);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTaskName()).isEqualTo("Test Task");
		assertThat(result.getId()).isEqualTo(1L);

		verify(modelMapper).map(dto, Task.class);
		verify(taskRepository).save(entity);
		verify(modelMapper).map(savedEntity, TaskResponseDto.class);
	}

	@Test
	void getTaskById_ShouldReturnTask_WhenTaskExists() {
		// Arrange
		Long taskId = 1L;
		Task entity = new Task();
		entity.setId(taskId);
		entity.setTaskName("Existing Task");

		TaskResponseDto responseDto = new TaskResponseDto();
		responseDto.setId(taskId);
		responseDto.setTaskName("Existing Task");

		when(taskRepository.findById(taskId)).thenReturn(Optional.of(entity));
		when(modelMapper.map(entity, TaskResponseDto.class)).thenReturn(responseDto);

		// Act
		TaskResponseDto result = taskService.getTaskById(taskId);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(taskId);
		assertThat(result.getTaskName()).isEqualTo("Existing Task");

		verify(taskRepository).findById(taskId);
		verify(modelMapper).map(entity, TaskResponseDto.class);
	}

	@Test
	void getTaskById_ShouldThrowException_WhenTaskNotFound() {
		// Arrange
		Long taskId = 999L;
		when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(taskId));

		verify(taskRepository).findById(taskId);
	}

	@Test
	void deleteTask_ShouldDeleteTask_WhenTaskExists() {
		// Arrange
		Long taskId = 1L;
		Task entity = new Task();
		entity.setId(taskId);
		entity.setTaskName("Task to Delete");

		when(taskRepository.findById(taskId)).thenReturn(Optional.of(entity));

		// Act
		taskService.deleteTask(taskId);

		// Assert
		verify(taskRepository).findById(taskId);
		verify(taskRepository).delete(entity);
	}

	@Test
	void deleteTask_ShouldThrowException_WhenTaskNotFound() {
		// Arrange
		Long taskId = 999L;
		when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(taskId));

		verify(taskRepository).findById(taskId);
	}
}

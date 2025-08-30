package com.project_management.dto;

import java.time.LocalDate;

import com.project_management.entities.Priority;
import com.project_management.entities.Status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Valid
public class TaskRequestDto {
	@NotBlank(message = "Task name is required")
	@Size(max = 30, message = "Task name must not exceed 30 characters")
	private String taskName;

	@NotBlank(message = "Description is required")
	@Size(max = 255, message = "Description must not exceed 255 characters")
	private String description;

	@NotNull(message = "Status is required")
	private Status status;

	@NotNull(message = "Priority is required")
	private Priority priority;

	@NotNull(message = "Due date is required")
	@Future(message = "Due date must be in the future")
	private LocalDate dueDate;
}

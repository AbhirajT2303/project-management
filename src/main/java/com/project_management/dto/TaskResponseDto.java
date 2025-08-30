package com.project_management.dto;

import java.time.LocalDate;

import com.project_management.entities.Priority;
import com.project_management.entities.Status;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponseDto {
	private Long id;
	private String taskName;
	private String description;
	private Status status;
	private Priority priority;
	private LocalDate dueDate;
}

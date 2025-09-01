package com.project_management.dto;

import java.time.LocalDate;

import com.project_management.entities.Priority;
import com.project_management.entities.TaskAction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Valid
public class TaskActionRequestDto {
	@NotNull(message = "Action is required")
	private TaskAction action;
	private String taskName;
	private String description;
	private Priority priority;
	private LocalDate dueDate;
	private String assignee;
	private String comments;
	private String reason;
	private String performedBy;

}

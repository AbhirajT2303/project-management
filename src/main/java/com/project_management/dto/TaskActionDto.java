package com.project_management.dto;

import com.project_management.entities.Status;
import com.project_management.entities.TaskAction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskActionDto {
	private TaskAction action;
	private String label;
	private Status nextStatus;

}

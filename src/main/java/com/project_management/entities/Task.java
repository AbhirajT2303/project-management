package com.project_management.entities;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Task extends BaseEntity {

	@Column(name = "task_name", length = 30, nullable = false)
	private String taskName;

	@Column(name = "description", length = 255, nullable = false)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 30, nullable = false)
	private Status status;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", length = 30, nullable = false)
	private Priority priority;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;
}

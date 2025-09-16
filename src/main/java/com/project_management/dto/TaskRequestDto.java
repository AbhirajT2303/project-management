package com.project_management.dto;

import com.project_management.entities.Priority;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Valid
public class TaskRequestDto {
    private String taskName;
    private String description;
    private Priority priority;
    private LocalDate dueDate;
}

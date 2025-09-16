package com.project_management.dto;

import com.project_management.entities.Priority;
import com.project_management.entities.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class TaskSearchDto {
    private String taskName;
    private String description;
    private Status status;
    private Priority priority;
    private String assignee;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDateTo;

    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private String sortDir = "desc";
}

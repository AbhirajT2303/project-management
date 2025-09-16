package com.project_management.dto;

import com.project_management.entities.Status;
import com.project_management.entities.TaskAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskActionHistoryDto {
    private Long id;
    private Long taskId;
    private TaskAction action;
    private Status fromStatus;
    private Status toStatus;
    private String performedBy;
    private LocalDateTime performedAt;
    private String comments;
    private String reason;
}

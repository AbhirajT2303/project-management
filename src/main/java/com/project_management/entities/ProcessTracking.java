package com.project_management.entities;

import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "process_tracking")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ProcessTracking {
    @Id
    @Column(name = "process_id", nullable = false, unique = true)
    private UUID processId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "total_rows_found", nullable = false, columnDefinition = "int default 0")
    private int totalRowsFound;

    @Column(name = "rows_with_data", nullable = false, columnDefinition = "int default 0")
    private int rowsWithData;

    @Column(name = "total_rows_saved", nullable = false, columnDefinition = "int default 0")
    private int totalRowsSaved;

    @Column(name = "total_rows_skipped", nullable = false, columnDefinition = "int default 0")
    private int totalRowsSkipped;

    @Column(name = "rows_processed", nullable = false, columnDefinition = "int default 0")
    private int rowsProcessed;

    @Lob
    @Column(name = "detected_column_mappings", columnDefinition = "TEXT")
    @JsonRawValue
    private String detectedColumnMappingsJson;

    @Column(name = "ai_mapping_used", nullable = false, columnDefinition = "boolean default false")
    private boolean aiMappingUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Lob
    @Column(name = "error_messages", columnDefinition = "TEXT")
    private String errorMessages;

    @Lob
    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;

    public double getProgressPercentage() {
        if (totalRowsFound == 0) return 0.0;
        return (double) rowsProcessed / totalRowsFound * 100.0;
    }

    public boolean isCompleted() {
        return status == ProcessStatus.COMPLETED || status == ProcessStatus.FAILED;
    }

    @PrePersist
    public void prePersist() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ProcessStatus.STARTED;
        }
    }
}

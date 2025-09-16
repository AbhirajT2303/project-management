package com.project_management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", length = 30, nullable = false)
    private String taskName;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 30, nullable = false)
    private Priority priority;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "assignee", length = 100)
    private String assignee;

    @Column(name = "last_status_change")
    private LocalDateTime lastStatusChange;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("performedAt DESC")
    private List<TaskActionHistory> actionHistory = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", nullable = false)
    private Tenant tenant;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = Status.NEW;
        }
        lastStatusChange = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastStatusChange = LocalDateTime.now();
    }

    public boolean isFinalState() {
        return status == Status.COMPLETED || status == Status.REJECTED;
    }

    public boolean canBeAssigned() {
        return status == Status.NEW || status == Status.ON_HOLD;
    }

    public boolean isInProgress() {
        return status == Status.IN_PROGRESS;
    }

    public String getAssigneeDisplayName() {
        return assignee != null ? assignee : "Unassigned";
    }

    public TaskActionHistory getLastAction() {
        return actionHistory.isEmpty() ? null : actionHistory.get(0);
    }

    public void setTenantId(UUID tenantId) {
        if (this.tenant == null) {
            this.tenant = new Tenant();
        }
        this.tenant.setId(tenantId);
    }

    public UUID getTenantUuid() {
        return this.tenant != null ? this.tenant.getId() : null;
    }
}

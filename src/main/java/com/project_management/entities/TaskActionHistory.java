package com.project_management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_action_history")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class TaskActionHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private TaskAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false)
    private Status fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private Status toStatus;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "reason", length = 500)
    private String reason;

    @PrePersist
    protected void onCreate() {
        if (performedAt == null) {
            performedAt = LocalDateTime.now();
        }
        if (tenant == null && task != null) {
            tenant = task.getTenant();
        }
    }

    public void setTenantId(UUID tenantId) {
        if (this.tenant == null) {
            this.tenant = new Tenant();
        }
        this.tenant.setId(tenantId);
    }

    public UUID getTenantId() {
        return this.tenant != null ? this.tenant.getId() : null;
    }
}

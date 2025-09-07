package com.project_management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class Tenant extends BaseEntity {

    @Id
    @Column(name = "tenant_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_name", nullable = false, length = 100)
    private String tenantName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "tenant",
            cascade = {CascadeType.REMOVE, CascadeType.PERSIST},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "tenant",
            cascade = {CascadeType.REMOVE, CascadeType.PERSIST},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<TaskActionHistory> taskActionHistory = new ArrayList<>();
}

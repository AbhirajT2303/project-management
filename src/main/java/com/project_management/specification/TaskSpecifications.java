package com.project_management.specification;

import com.project_management.context.TenantContext;
import com.project_management.entities.Priority;
import com.project_management.entities.Status;
import com.project_management.entities.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskSpecifications {

    public static Specification<Task> withDynamicSearch(
            String taskName,
            String description,
            Status status,
            Priority priority,
            String assignee,
            LocalDate dueDateFrom,
            LocalDate dueDateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            UUID currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null) {
                predicates.add(cb.equal(root.get("tenant").get("id"), currentTenant));
            }

            if (taskName != null && !taskName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("taskName")), "%" + taskName.toLowerCase().trim() + "%"));
            }

            if (description != null && !description.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase().trim() + "%"));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (assignee != null && !assignee.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("assignee")),
                        "%" + assignee.toLowerCase().trim() + "%"));
            }

            if (dueDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }

            if (dueDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Task> hasStatus(Status status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            UUID currentTenant = TenantContext.getCurrentTenant();

            if (currentTenant != null) {
                predicates.add(cb.equal(root.get("tenant").get("id"), currentTenant));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            UUID currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null) {
                predicates.add(cb.equal(root.get("tenant").get("id"), currentTenant));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

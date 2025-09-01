package com.project_management.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "task_action_history")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class TaskActionHistory extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

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
	}
}

package com.project_management.entities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum TaskAction {

	ASSIGN_TO_TEAM(Status.NEW, Status.ASSIGNED), ADD_DETAILS(Status.NEW, Status.REJECTED),

	START_WORKING(Status.ASSIGNED, Status.IN_PROGRESS), DELEGATE(Status.ASSIGNED, Status.ON_HOLD),
	REQUEST_INFO(Status.ASSIGNED, Status.REJECTED),

	UPDATE_PROGRESS(Status.IN_PROGRESS, Status.FOLLOW_UP), COLLABORATE(Status.IN_PROGRESS, Status.COMPLETED),
	BLOCKED(Status.IN_PROGRESS, Status.ON_HOLD),

	REQUEST_UPDATE(Status.FOLLOW_UP, Status.IN_PROGRESS), ESCALATE(Status.FOLLOW_UP, Status.ON_HOLD),
	SEND_REMINDER(Status.FOLLOW_UP, Status.COMPLETED),

	RESUME(Status.ON_HOLD, Status.IN_PROGRESS), REASSIGN(Status.ON_HOLD, Status.ASSIGNED),
	CANCEL(Status.ON_HOLD, Status.REJECTED),

	CLOSE_TASK(Status.COMPLETED, Status.COMPLETED), ARCHIVE(Status.COMPLETED, Status.COMPLETED),

	CANCEL_TASK(Status.REJECTED, Status.REJECTED), PROVIDE_REASON(Status.REJECTED, Status.REJECTED);

	private final Status fromStatus;
	private final Status toStatus;

	TaskAction(Status fromStatus, Status toStatus) {
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
	}

	public Status getFromStatus() {
		return fromStatus;
	}

	public Status getToStatus() {
		return toStatus;
	}

	public static List<TaskAction> getValidActionsForStatus(Status status) {
		return Arrays.stream(values()).filter(action -> action.fromStatus == status).collect(Collectors.toList());
	}

	public static boolean isActionValidForStatus(TaskAction action, Status currentStatus) {
		return action.getFromStatus() == currentStatus;
	}
}

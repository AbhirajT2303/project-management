package com.project_management.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.project_management.context.TenantContext;
import com.project_management.entities.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.project_management.dto.TaskActionDto;
import com.project_management.dto.TaskActionRequestDto;
import com.project_management.dto.TaskResponseDto;
import com.project_management.exception.ResourceNotFoundException;
import com.project_management.repository.TaskActionHistoryRepository;
import com.project_management.repository.TaskRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskActionServiceImpl implements TaskActionService {

	private final TaskRepository taskRepository;
	private final TaskActionHistoryRepository actionHistoryRepository;
	private final ModelMapper modelMapper;
	private final WorkflowRuleService workflowRule;

	@Override
	public TaskResponseDto executeAction(Long taskId, TaskActionRequestDto request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        Task task = taskRepository.findByIdAndTenant_Id(taskId,tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

		String currentStatus = task.getStatus().name();
		String actionName = request.getAction().name();

		if (workflowRule.isFinalSate(currentStatus)) {
			throw new IllegalStateException("Task is in final state and cannot be modified: " + currentStatus);
		}
		if (!workflowRule.isValidAction(currentStatus, actionName)) {
			Set<String> validActions = workflowRule.getValidActions(currentStatus);
			throw new IllegalStateException("Action '" + actionName + "' not valid. \nValid actions:  " + validActions);
		}
		String nextStatusName = workflowRule.getNextStatus(currentStatus, actionName);
		Status nextStatus = Status.valueOf(nextStatusName);

		TaskActionHistory history = new TaskActionHistory();
		history.setTask(task);
        history.setTenantId(tenantId);
		history.setAction(request.getAction());
		history.setFromStatus(task.getStatus());
		history.setToStatus(nextStatus);
		history.setPerformedBy(request.getPerformedBy());
		history.setComments(request.getComments());
		history.setReason(request.getReason());

		actionHistoryRepository.save(history);

		task.setStatus(nextStatus);
		if (request.getAssignee() != null) {
			task.setAssignee(request.getAssignee());
		}
		Task savedTask = taskRepository.save(task);

		TaskResponseDto responseDto = modelMapper.map(savedTask, TaskResponseDto.class);
		Set<String> nextValidActionName = workflowRule.getValidActions(nextStatusName);
		List<TaskAction> availableActions = nextValidActionName.stream().map(TaskAction::valueOf)
				.collect(Collectors.toList());
		responseDto.setAvailableActions(availableActions);
		return responseDto;
	}

	private List<TaskActionDto> getAvailableActionsForStatus(Status status) {
		return TaskAction.getValidActionsForStatus(status).stream()
				.map(action -> new TaskActionDto(action, getActionLabel(action), action.getToStatus()))
				.collect(Collectors.toList());
	}

	private String getActionLabel(TaskAction action) {
		switch (action) {
		case ASSIGN_TO_TEAM:
			return "Assign to team";
		case ADD_DETAILS:
			return "Add details";
		case START_WORKING:
			return "Start working";
		case DELEGATE:
			return "Delegate";
		case REQUEST_INFO:
			return "Request info";
		case UPDATE_PROGRESS:
			return "Update progress";
		case COLLABORATE:
			return "Collaborate";
		case BLOCKED:
			return "Blocked";
		case REQUEST_UPDATE:
			return "Request update";
		case ESCALATE:
			return "Escalate";
		case SEND_REMINDER:
			return "Send reminder";
		case RESUME:
			return "Resume";
		case REASSIGN:
			return "Reassign";
		case CANCEL:
			return "Cancel";
		case CLOSE_TASK:
			return "Close task";
		case ARCHIVE:
			return "Archive";
		case CANCEL_TASK:
			return "Cancel task";
		case PROVIDE_REASON:
			return "Provide reason";
		default:
			return action.name();
		}
	}
}

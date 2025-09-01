package com.project_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.project_management.dto.TaskActionDto;
import com.project_management.dto.TaskActionRequestDto;
import com.project_management.dto.TaskResponseDto;
import com.project_management.entities.Status;
import com.project_management.entities.Task;
import com.project_management.entities.TaskAction;
import com.project_management.entities.TaskActionHistory;
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

	@Override
	public TaskResponseDto executeAction(Long taskId, TaskActionRequestDto request) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

		if (!TaskAction.isActionValidForStatus(request.getAction(), task.getStatus())) {

			List<TaskAction> validActions = TaskAction.getValidActionsForStatus(task.getStatus());
			throw new ResourceNotFoundException("Action '" + request.getAction() + "' is not valid for task in '"
					+ task.getStatus() + "' status. Valid actions: " + validActions);
		}

		TaskActionHistory history = new TaskActionHistory();
		history.setTask(task);
		history.setAction(request.getAction());
		history.setFromStatus(task.getStatus());
		history.setToStatus(request.getAction().getToStatus());
		history.setPerformedBy(request.getPerformedBy());
		history.setComments(request.getComments());
		history.setReason(request.getReason());

		actionHistoryRepository.save(history);
		task.setStatus(request.getAction().getToStatus());
		Task savedTask = taskRepository.save(task);
		TaskResponseDto responseDto = modelMapper.map(savedTask, TaskResponseDto.class);

		// Add available actions for current status
		responseDto.setAvailableActions(TaskAction.getValidActionsForStatus(savedTask.getStatus()));

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

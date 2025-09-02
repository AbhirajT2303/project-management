package com.project_management.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.project_management.config.WorkflowConfig;
import com.project_management.config.WorkflowConfigLoader;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WorkflowRuleService {
	private final WorkflowConfigLoader configLoader;

	public boolean isValidAction(String currentStatus, String action) {
		WorkflowConfig config = configLoader.getConfig();
		Map<String, String> allowedActions = config.getTransitions().get(currentStatus);

		return allowedActions != null && allowedActions.containsKey(action);
	}

	public String getNextStatus(String currentStatus, String action) {
		WorkflowConfig config = configLoader.getConfig();
		Map<String, String> allowedActions = config.getTransitions().get(currentStatus);

		if (allowedActions == null) {
			throw new IllegalStateException("No transitions defined for status: " + currentStatus);
		}
		String nextStatus = allowedActions.get(action);
		if (nextStatus == null) {
			throw new IllegalStateException("Action '" + action + "' not allowed for status: " + currentStatus);
		}
		return nextStatus;
	}

	public Set<String> getValidActions(String currentStatus) {
		WorkflowConfig config = configLoader.getConfig();
		Map<String, String> allowedAction = config.getTransitions().get(currentStatus);
		return allowedAction != null ? allowedAction.keySet() : Collections.emptySet();
	}

	public boolean isFinalSate(String status) {
		WorkflowConfig config = configLoader.getConfig();
		return config.getFinalStates().contains(status);
	}
}

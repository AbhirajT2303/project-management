package com.project_management.config;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkflowConfig {
	private List<String> finalStates;
	private Map<String, Map<String, String>> transitions;
}

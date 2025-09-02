package com.project_management.config;

import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Component
public class WorkflowConfigLoader {

	private WorkflowConfig workflowConfig;

	@PostConstruct
	public void loadConfig() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			InputStream stream = getClass().getResourceAsStream("/workflow-config.json");
			this.workflowConfig = mapper.readValue(stream, WorkflowConfig.class);
		} catch (Exception e) {
			throw new RuntimeException("Cannot load workflow configuration", e);
		}
	}

	public WorkflowConfig getConfig() {
		return this.workflowConfig;
	}
}

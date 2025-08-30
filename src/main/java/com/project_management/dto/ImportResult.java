package com.project_management.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ImportResult {
	private final int totalRows;
	private final int rowsWithData;
	private final int rowsSaved;
	private final int rowsSkipped;
	private final String fileName;
	private Map<String, String> detectedColumnMappings; 
    private boolean aiMappingUsed; 
}

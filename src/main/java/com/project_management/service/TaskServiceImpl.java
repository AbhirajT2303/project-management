package com.project_management.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project_management.dto.ImportResult;
import com.project_management.dto.TaskRequestDto;
import com.project_management.dto.TaskResponseDto;
import com.project_management.entities.Task;
import com.project_management.exception.ResourceNotFoundException;
import com.project_management.repository.TaskRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

	private final TaskRepository taskRepository;
	private final ModelMapper modelMapper;
	private final DateTimeFormatter[] dateFormatters = { DateTimeFormatter.ofPattern("yyyy-MM-dd"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("MM/dd/yyyy"),
			DateTimeFormatter.ofPattern("dd-MM-yyyy"), DateTimeFormatter.ofPattern("MM-dd-yyyy") };

	@Override
	public TaskResponseDto createTask(TaskRequestDto taskRequestDto) {
		log.info("Creating new task:{}", taskRequestDto.getTaskName());

		Task task = modelMapper.map(taskRequestDto, Task.class);
		Task savedTask = taskRepository.save(task);
		return modelMapper.map(savedTask, TaskResponseDto.class);
	}

	@Override
	public TaskResponseDto updateTask(Long taskId, TaskRequestDto taskRequestDto) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

		modelMapper.map(taskRequestDto, task);

		Task updatedTask = taskRepository.save(task);

		return modelMapper.map(updatedTask, TaskResponseDto.class);
	}

	@Override
	public TaskResponseDto getTaskById(Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

		return modelMapper.map(task, TaskResponseDto.class);
	}

	@Override
	public List<TaskResponseDto> getAllTasks() {

		return taskRepository.findAll().stream().map(task -> modelMapper.map(task, TaskResponseDto.class))
				.collect(Collectors.toList());
	}

	@Override
	public boolean deleteTask(Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
		taskRepository.delete(task);
		return true;
	}

	@Override
	public ImportResult importCsv(MultipartFile file) {
		String fileName = file.getOriginalFilename();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

			Set<String> fileHeaders = csvParser.getHeaderMap().keySet();
			log.info("CSV file headers detected: {}", fileHeaders);

			Set<String> taskFields = getTaskEntityFields();
			log.info("Task entity fields: {}", taskFields);

			Map<String, String> columnMapping = createColumnMapping(fileHeaders, taskFields);
			log.info("Column mapping created: {}", columnMapping);

			if (columnMapping.isEmpty()) {
				throw new RuntimeException("No matching columns found between CSV file and Task entity. "
						+ "Please check column names in your CSV file.");
			}

			List<Task> validTasks = new ArrayList<>();
			int totalRows = 0;
			int rowsWithData = 0;

			for (CSVRecord csvRecord : csvParser) {
				totalRows++;

				Task task = new Task();
				boolean hasValidData = false;

				for (Map.Entry<String, String> mapping : columnMapping.entrySet()) {
					String csvColumn = mapping.getKey();
					String taskField = mapping.getValue();

					try {
						String cellValue = csvRecord.get(csvColumn);
						if (cellValue != null && !cellValue.trim().isEmpty()) {
							boolean fieldSet = setTaskFieldValue(task, taskField, cellValue.trim());
							if (fieldSet) {
								hasValidData = true;
							}
						}
					} catch (Exception e) {
						log.warn("Error processing row {} column '{}': {}", totalRows, csvColumn, e.getMessage());
					}
				}

				if (hasValidData) {
					validTasks.add(task);
					rowsWithData++;
				}
			}

			List<Task> filteredTasks = validTasks.stream().filter(task -> !hasNullFields(task))
					.collect(Collectors.toList());

			List<Task> savedTasks = taskRepository.saveAll(filteredTasks);

			int rowsSaved = savedTasks.size();
			int rowsSkipped = totalRows - rowsSaved;

			log.info("CSV import completed - Total rows: {}, Rows with data: {}, Rows saved: {}, Rows skipped: {}",
					totalRows, rowsWithData, rowsSaved, rowsSkipped);

			return new ImportResult(totalRows, rowsWithData, rowsSaved, rowsSkipped, fileName);

		} catch (IOException e) {
			log.error("Error processing CSV file: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error during CSV import: {}", e.getMessage(), e);
			throw new RuntimeException("CSV import failed: " + e.getMessage());
		}
	}

	@Override
	public int importExcel(MultipartFile file) {
		return 0;
	}

	private Set<String> getTaskEntityFields() {
		return Arrays.stream(Task.class.getDeclaredFields()).map(Field::getName).filter(name -> !name.equals("id"))
				.collect(Collectors.toSet());
	}

	private boolean hasNullFields(Task task) {
		return task.getTaskName() == null || task.getDescription() == null || task.getStatus() == null
				|| task.getDueDate() == null || task.getPriority() == null;
	}

	private Map<String, String> createColumnMapping(Set<String> fileHeaders, Set<String> taskFields) {
		Map<String, String> mapping = new HashMap<>();

		for (String fileHeader : fileHeaders) {
			String normalizedFileHeader = normalizeColumnName(fileHeader);

			for (String taskField : taskFields) {
				String normalizedTaskField = normalizeColumnName(taskField);

				if (normalizedFileHeader.equals(normalizedTaskField)) {
					mapping.put(fileHeader, taskField);
					log.debug("Mapped CSV column '{}' to Task field '{}'", fileHeader, taskField);
					break;
				}
			}
		}

		for (String fileHeader : fileHeaders) {
			if (!mapping.containsKey(fileHeader)) {
				log.warn("CSV column '{}' could not be mapped to any Task field", fileHeader);
			}
		}

		return mapping;
	}

	private String normalizeColumnName(String columnName) {
		return columnName.toLowerCase().replaceAll("[\\s_-]+", "").replaceAll("[^a-z0-9]", "");
	}

	private boolean setTaskFieldValue(Task task, String fieldName, String value) {
		try {
			Field field = Task.class.getDeclaredField(fieldName);
			field.setAccessible(true);

			Object convertedValue = convertStringToFieldType(field.getType(), value);
			if (convertedValue != null) {
				field.set(task, convertedValue);
				log.debug("Set Task.{} = '{}'", fieldName, value);
				return true;
			}

		} catch (NoSuchFieldException e) {
			log.warn("Field '{}' not found in Task entity", fieldName);
		} catch (Exception e) {
			log.warn("Could not set field '{}' with value '{}': {}", fieldName, value, e.getMessage());
		}

		return false;
	}

	private Object convertStringToFieldType(Class<?> fieldType, String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		value = value.trim();

		try {
			if (fieldType == String.class) {
				return value;
			} else if (fieldType == Long.class || fieldType == long.class) {
				return Long.parseLong(value);
			} else if (fieldType == Integer.class || fieldType == int.class) {
				return Integer.parseInt(value);
			} else if (fieldType == Boolean.class || fieldType == boolean.class) {
				return Boolean.parseBoolean(value);
			} else if (fieldType == LocalDate.class) {
				return parseDate(value);
			} else if (fieldType.isEnum()) {
				return Enum.valueOf((Class<Enum>) fieldType, value.toUpperCase());
			} else {
				log.warn("Unsupported field type: {}. Using string value.", fieldType.getSimpleName());
				return value;
			}
		} catch (Exception e) {
			log.warn("Failed to convert '{}' to {}: {}", value, fieldType.getSimpleName(), e.getMessage());
			return null;
		}
	}

	private LocalDate parseDate(String dateStr) {
		for (DateTimeFormatter formatter : dateFormatters) {
			try {
				return LocalDate.parse(dateStr, formatter);
			} catch (Exception e) {
			}
		}
		throw new RuntimeException("Unable to parse date: " + dateStr
				+ ". Supported formats: yyyy-MM-dd, dd/MM/yyyy, MM/dd/yyyy, dd-MM-yyyy, MM-dd-yyyy");
	}
}

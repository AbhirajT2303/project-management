package com.project_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project_management.context.TenantContext;
import com.project_management.dto.ImportResult;
import com.project_management.dto.TaskRequestDto;
import com.project_management.dto.TaskResponseDto;
import com.project_management.entities.Task;
import com.project_management.exception.ResourceNotFoundException;
import com.project_management.repository.TaskRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;
    private final OpenAiService openAiService;
    private final DateTimeFormatter[] dateFormatters = {DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"), DateTimeFormatter.ofPattern("MM-dd-yyyy")};
    private final TenantService tenantService;
    boolean aiMappingUsed = false;

    @Override
    public TaskResponseDto createTask(TaskRequestDto taskRequestDto) {
        log.info("Creating new task:{}", taskRequestDto.getTaskName());
        UUID tenantId = TenantContext.getCurrentTenant();
        log.info("in createTask: "+ tenantId);
        if (!tenantService.existsById(tenantId)) {
            throw new ResourceNotFoundException("Active tenant not found with id:" + tenantId);
        }
        Task task = modelMapper.map(taskRequestDto, Task.class);
        task.setTenantId(tenantId);
        Task savedTask = taskRepository.save(task);
        return modelMapper.map(savedTask, TaskResponseDto.class);
    }

    @Override
    public TaskResponseDto updateTask(Long taskId, TaskRequestDto taskRequestDto) {
        UUID tenantId = TenantContext.getCurrentTenant();

        Task task = taskRepository.findByIdAndTenant_Id(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId + " for tenant: " + tenantId));

        modelMapper.map(taskRequestDto, task);

        Task updatedTask = taskRepository.save(task);

        return modelMapper.map(updatedTask, TaskResponseDto.class);
    }

    @Override
    public TaskResponseDto getTaskById(Long taskId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Task task = taskRepository.findByIdAndTenant_Id(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId + " for tenant: " + tenantId));

        return modelMapper.map(task, TaskResponseDto.class);
    }

    @Override
    public List<TaskResponseDto> getAllTasks() {
        UUID tenantId = TenantContext.getCurrentTenant();

        return taskRepository.findAllByTenant_Id(tenantId).stream().map(task -> modelMapper.map(task, TaskResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteTask(Long taskId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        Task task = taskRepository.findByIdAndTenant_Id(taskId,tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        taskRepository.delete(task);
        return true;
    }

    @Override
    public ImportResult importCsv(MultipartFile file) {
        String fileName = file.getOriginalFilename();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            Map<String, Integer> originalHeaderMap = csvParser.getHeaderMap();
            Set<String> originalHeaders = originalHeaderMap.keySet();

            Set<String> trimmedHeaders = originalHeaders.stream().map(String::trim).collect(Collectors.toSet());

            Map<String, String> headerLookupMap = originalHeaderMap.keySet().stream()
                    .collect(Collectors.toMap(header -> header.trim(), header -> header));

            log.info("CSV file headers detected (original): {}", originalHeaderMap.keySet());
            log.info("CSV file headers detected (trimmed): {}", trimmedHeaders);

            Set<String> taskFields = getTaskEntityFields();
            log.info("Task entity fields: {}", taskFields);

            Map<String, String> columnMapping = createColumnMapping(trimmedHeaders, taskFields);
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
                    String trimmedCsvColumn = mapping.getKey();
                    String taskField = mapping.getValue();

                    try {
                        String originalCsvColumn = headerLookupMap.get(trimmedCsvColumn);
                        if (originalCsvColumn != null) {
                            String cellValue = csvRecord.get(originalCsvColumn);
                            if (cellValue != null && !cellValue.trim().isEmpty()) {
                                boolean fieldSet = setTaskFieldValue(task, taskField, cellValue.trim());
                                if (fieldSet) {
                                    hasValidData = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error processing row {} column '{}': {}", totalRows, trimmedCsvColumn,
                                e.getMessage());
                    }
                }

                if (hasValidData) {
                    validTasks.add(task);
                    rowsWithData++;
                }
            }

            List<Task> filteredTasks = validTasks.stream().filter(task -> !hasNullFields(task))
                    .collect(Collectors.toList());
            setTenantForImportedTasks(filteredTasks);
            List<Task> savedTasks = taskRepository.saveAll(filteredTasks);

            int rowsSaved = savedTasks.size();
            int rowsSkipped = totalRows - rowsSaved;

            log.info("CSV import completed - Total rows: {}, Rows with data: {}, Rows saved: {}, Rows skipped: {}",
                    totalRows, rowsWithData, rowsSaved, rowsSkipped);

            return new ImportResult(totalRows, rowsWithData, rowsSaved, rowsSkipped, fileName, columnMapping,
                    aiMappingUsed);

        } catch (IOException e) {
            log.error("Error processing CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during CSV import: {}", e.getMessage(), e);
            throw new RuntimeException("CSV import failed: " + e.getMessage());
        }
    }

    @Override
    public ImportResult importExcel(MultipartFile file) {
        String fileName = file.getOriginalFilename();

        try {
            Workbook workbook;

            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(file.getInputStream());
            } else {
                throw new RuntimeException("Unsupported Excel format. Only .xls and .xlsx files are supported.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            Map<String, Integer> originalFileHeaders = new HashMap<>();
            Map<String, String> headerLookupMap = new HashMap<>();

            if (rows.hasNext()) {
                Row headerRow = rows.next();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        String originalHeaderName = getCellValueAsString(cell);
                        if (!originalHeaderName.isEmpty()) {
                            String trimmedHeaderName = originalHeaderName.trim();
                            originalFileHeaders.put(originalHeaderName, i);
                            headerLookupMap.put(trimmedHeaderName, originalHeaderName);
                        }
                    }
                }
            }

            Set<String> trimmedHeaders = headerLookupMap.keySet();

            log.info("Excel file headers detected (original): {}", originalFileHeaders.keySet());
            log.info("Excel file headers detected (trimmed): {}", trimmedHeaders);

            Set<String> taskFields = getTaskEntityFields();
            log.info("Task entity fields: {}", taskFields);

            Map<String, String> columnMapping = createColumnMapping(trimmedHeaders, taskFields);
            log.info("Column mapping created: {}", columnMapping);

            if (columnMapping.isEmpty()) {
                workbook.close();
                throw new RuntimeException("No matching columns found between Excel file and Task entity. "
                        + "Please check column names in your Excel file.");
            }

            List<Task> validTasks = new ArrayList<>();
            int totalRows = 0;
            int rowsWithData = 0;

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                totalRows++;

                Task task = new Task();
                boolean hasValidData = false;

                for (Map.Entry<String, String> mapping : columnMapping.entrySet()) {
                    String trimmedExcelColumn = mapping.getKey();
                    String taskField = mapping.getValue();

                    try {
                        String originalExcelColumn = headerLookupMap.get(trimmedExcelColumn);
                        if (originalExcelColumn != null) {
                            Integer columnIndex = originalFileHeaders.get(originalExcelColumn);
                            if (columnIndex != null) {
                                Cell cell = currentRow.getCell(columnIndex);
                                String cellValue = getCellValueAsString(cell);

                                if (!cellValue.isEmpty()) {
                                    boolean fieldSet = setTaskFieldValue(task, taskField, cellValue);
                                    if (fieldSet) {
                                        hasValidData = true;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error processing Excel row {} column '{}': {}", totalRows, trimmedExcelColumn,
                                e.getMessage());
                    }
                }

                if (hasValidData) {
                    validTasks.add(task);
                    rowsWithData++;
                }
            }

            workbook.close();

            List<Task> filteredTasks = validTasks.stream().filter(task -> !hasNullFields(task))
                    .collect(Collectors.toList());

            setTenantForImportedTasks(filteredTasks);
            List<Task> savedTasks = taskRepository.saveAll(filteredTasks);

            int rowsSaved = savedTasks.size();
            int rowsSkipped = totalRows - rowsSaved;

            log.info("Excel import completed - Total rows: {}, Rows with data: {}, Rows saved: {}, Rows skipped: {}",
                    totalRows, rowsWithData, rowsSaved, rowsSkipped);

            return new ImportResult(totalRows, rowsWithData, rowsSaved, rowsSkipped, fileName, columnMapping,
                    aiMappingUsed);

        } catch (IOException e) {
            log.error("Error processing Excel file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during Excel import: {}", e.getMessage(), e);
            throw new RuntimeException("Excel import failed: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    LocalDate date = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return date.toString();
                }
                double numericValue = cell.getNumericCellValue();

                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return getCellValueAsString(cell);
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
            case _NONE:
            default:
                return "";
        }
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

        Set<String> unmapped = fileHeaders.stream().filter(h -> !mapping.containsKey(h)).collect(Collectors.toSet());

        if (!unmapped.isEmpty()) {
            aiMappingUsed = true;
            log.info("Unmapped headers: {}", unmapped);
            try {
                ChatCompletionRequest request = ChatCompletionRequest.builder().model("gpt-4o-mini")
                        .messages(List.of(new ChatMessage("system", "You output only valid JSON."),
                                new ChatMessage("user",
                                        String.format(
                                                "Match these Excel/CSV headers: %s to these task fields: %s. "
                                                        + "Return a JSON object {\"header\": \"taskField\"}.",
                                                unmapped, taskFields))))
                        .maxTokens(300).temperature(0.0).build();

                ChatCompletionResult result = openAiService.createChatCompletion(request);

                String jsonMapping = result.getChoices().get(0).getMessage().getContent().trim();

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> aiMapping = objectMapper.readValue(jsonMapping, Map.class);

                mapping.putAll(aiMapping);
                log.info("AI mapping result: {}", jsonMapping);

            } catch (Exception e) {
                log.error("AI column mapping failed: {}", e.getMessage(), e);
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
    private void setTenantForImportedTasks(List<Task> tasks) {
        UUID tenantId = TenantContext.getCurrentTenant();
        tasks.forEach(task -> task.setTenantId(tenantId));
    }
}

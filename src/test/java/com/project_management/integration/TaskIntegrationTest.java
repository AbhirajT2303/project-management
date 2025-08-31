package com.project_management.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project_management.dto.ApiResponse;
import com.project_management.dto.TaskRequestDto;
import com.project_management.entities.Priority;
import com.project_management.entities.Status;
import com.project_management.repository.TaskRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = { "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop" })
public class TaskIntegrationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}

	// ========== TASK CREATION TESTS ==========

	@Test
	void createTask_ShouldCreateAndReturnTask_WhenValidData() {
		TaskRequestDto taskRequest = new TaskRequestDto();
		taskRequest.setTaskName("Implement testing");
		taskRequest.setDescription("Test description");
		taskRequest.setPriority(Priority.HIGH);
		taskRequest.setStatus(Status.TODO);
		taskRequest.setDueDate(LocalDate.of(2025, 12, 31));

		ResponseEntity<ApiResponse> response = restTemplate.postForEntity(createURLWithPort("/api/v1/tasks"),
				taskRequest, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody().isSuccess()).isTrue();
		assertThat(response.getBody().getMessage()).contains("Task Created successfully");

		List<com.project_management.entities.Task> tasks = taskRepository.findAll();
		assertThat(tasks).hasSize(1);
		assertThat(tasks.get(0).getTaskName()).isEqualTo("Implement testing");
	}

	@Test
	void createTask_ShouldReturnBadRequest_WhenInvalidData() {
		TaskRequestDto invalidRequest = new TaskRequestDto();
		invalidRequest.setTaskName(""); // Invalid - empty
		invalidRequest.setDescription("Description");

		ResponseEntity<ApiResponse> response = restTemplate.postForEntity(createURLWithPort("/api/v1/tasks"),
				invalidRequest, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().isSuccess()).isFalse();
	}

	@Test
	void createTask_ShouldReturnBadRequest_WhenTaskNameTooLong() {
		TaskRequestDto taskRequest = new TaskRequestDto();
		taskRequest.setTaskName("A".repeat(31)); // Over 30 character limit
		taskRequest.setDescription("Valid description");
		taskRequest.setPriority(Priority.HIGH);
		taskRequest.setStatus(Status.TODO);
		taskRequest.setDueDate(LocalDate.now().plusDays(10));

		ResponseEntity<ApiResponse> response = restTemplate.postForEntity(createURLWithPort("/api/v1/tasks"),
				taskRequest, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void createTask_ShouldReturnBadRequest_WhenPastDueDate() {
		TaskRequestDto taskRequest = new TaskRequestDto();
		taskRequest.setTaskName("Valid Task");
		taskRequest.setDescription("Valid description");
		taskRequest.setPriority(Priority.HIGH);
		taskRequest.setStatus(Status.TODO);
		taskRequest.setDueDate(LocalDate.now().minusDays(1)); // Past date

		ResponseEntity<ApiResponse> response = restTemplate.postForEntity(createURLWithPort("/api/v1/tasks"),
				taskRequest, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void createTask_ShouldReturnBadRequest_WhenNullRequiredFields() {
		TaskRequestDto taskRequest = new TaskRequestDto();
		taskRequest.setTaskName("Valid Task");
		taskRequest.setDescription("Valid description");
		// Missing required fields: priority, status, dueDate

		ResponseEntity<ApiResponse> response = restTemplate.postForEntity(createURLWithPort("/api/v1/tasks"),
				taskRequest, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	// ========== TASK RETRIEVAL TESTS ==========

	@Test
	void getAllTasks_ShouldReturnAllTasks() {
		createTestTask("Task 1", "Description 1");
		createTestTask("Task 2", "Description 2");
		createTestTask("Task 3", "Description 3");

		ResponseEntity<ApiResponse> response = restTemplate.getForEntity(createURLWithPort("/api/v1/tasks"),
				ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().isSuccess()).isTrue();

		List<Map<String, Object>> tasks = (List<Map<String, Object>>) response.getBody().getData();
		assertThat(tasks).hasSize(3);
	}

	@Test
	void getAllTasks_ShouldReturnEmpty_WhenNoTasks() {
		ResponseEntity<ApiResponse> response = restTemplate.getForEntity(createURLWithPort("/api/v1/tasks"),
				ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) response.getBody().getData();
		assertThat(tasks).isEmpty();
	}

	@Test
	void getTask_ShouldReturnNotFound_WhenTaskDoesNotExist() {
		ResponseEntity<ApiResponse> response = restTemplate.getForEntity(createURLWithPort("/api/v1/tasks/999"),
				ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().isSuccess()).isFalse();
	}

	@Test
	void getTask_ShouldReturnTask_WhenTaskExists() {
		Long taskId = createTestTask("Existing Task", "Description");

		ResponseEntity<ApiResponse> response = restTemplate.getForEntity(createURLWithPort("/api/v1/tasks/" + taskId),
				ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().isSuccess()).isTrue();

		Map<String, Object> taskData = (Map<String, Object>) response.getBody().getData();
		assertThat(taskData.get("taskName")).isEqualTo("Existing Task");
	}

	// ========== TASK UPDATE TESTS ==========

	@Test
	void updateTask_ShouldUpdateAndReturnTask_WhenValidData() {
		Long taskId = createTestTask("Original Task", "Original Description");

		TaskRequestDto updateRequest = new TaskRequestDto();
		updateRequest.setTaskName("Updated Task");
		updateRequest.setDescription("Updated Description");
		updateRequest.setStatus(Status.IN_PROGRESS);
		updateRequest.setPriority(Priority.MEDIUM);
		updateRequest.setDueDate(LocalDate.of(2025, 11, 30));

		ResponseEntity<ApiResponse> response = restTemplate.exchange(createURLWithPort("/api/v1/tasks/" + taskId),
				HttpMethod.PUT, new HttpEntity<>(updateRequest), ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().isSuccess()).isTrue();

		Map<String, Object> taskData = (Map<String, Object>) response.getBody().getData();
		assertThat(taskData.get("taskName")).isEqualTo("Updated Task");
		assertThat(taskData.get("status")).isEqualTo("IN_PROGRESS");
	}

	@Test
	void updateTask_ShouldReturnNotFound_WhenTaskDoesNotExist() {
		TaskRequestDto updateRequest = new TaskRequestDto();
		updateRequest.setTaskName("Updated Task");
		updateRequest.setDescription("Updated Description");
		updateRequest.setStatus(Status.IN_PROGRESS);
		updateRequest.setPriority(Priority.MEDIUM);
		updateRequest.setDueDate(LocalDate.of(2025, 11, 30));

		ResponseEntity<ApiResponse> response = restTemplate.exchange(createURLWithPort("/api/v1/tasks/999"),
				HttpMethod.PUT, new HttpEntity<>(updateRequest), ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	// ========== TASK DELETION TESTS ==========

	@Test
	void deleteTask_ShouldDeleteTask_WhenTaskExists() {
		Long taskId = createTestTask("Task to Delete", "Description");

		ResponseEntity<ApiResponse> response = restTemplate.exchange(createURLWithPort("/api/v1/tasks/" + taskId),
				HttpMethod.DELETE, null, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().isSuccess()).isTrue();
		assertThat(response.getBody().getMessage()).contains("Task deleted successfully");
		assertThat(taskRepository.existsById(taskId)).isFalse();
	}

	@Test
	void deleteTask_ShouldReturnNotFound_WhenTaskDoesNotExist() {
		ResponseEntity<ApiResponse> response = restTemplate.exchange(createURLWithPort("/api/v1/tasks/999"),
				HttpMethod.DELETE, null, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	// ========== CSV UPLOAD TESTS ==========

	@Test
	void uploadCsvFile_ShouldImportTasks_WhenValidCsvFile() throws Exception {
		String csvContent = "Task Name,Description,Status,Priority,Due Date\n"
				+ "CSV Task 1,CSV Description 1,TODO,HIGH,2025-12-31\n"
				+ "CSV Task 2,CSV Description 2,TODO,MEDIUM,2025-11-30\n"
				+ "CSV Task 3,CSV Description 3,IN_PROGRESS,LOW,2025-12-25";

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(csvContent.getBytes()) {
			@Override
			public String getFilename() {
				return "test_tasks.csv";
			}
		});

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		ResponseEntity<ApiResponse> response = restTemplate.exchange(createURLWithPort("/api/v1/tasks/upload"),
				HttpMethod.POST, requestEntity, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().isSuccess()).isTrue();

		List<com.project_management.entities.Task> tasks = taskRepository.findAll();
		assertThat(tasks).hasSize(3);
	}

	@Test
	void uploadCsvFile_ShouldSkipInvalidRows_WhenMixedData() throws Exception {
		String csvContent = "Task Name,Description,Status,Priority,Due Date\n"
				+ "Valid Task,Valid Description,TODO,HIGH,2025-12-31\n"
				+ "Invalid Task,,INVALID_STATUS,INVALID_PRIORITY,invalid-date\n"
				+ "Another Valid,Another Description,TODO,LOW,2025-11-30";

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(csvContent.getBytes()) {
			@Override
			public String getFilename() {
				return "mixed_data.csv";
			}
		});

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		ResponseEntity<ApiResponse> response = restTemplate.exchange(createURLWithPort("/api/v1/tasks/upload"),
				HttpMethod.POST, requestEntity, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().isSuccess()).isTrue();

		List<com.project_management.entities.Task> tasks = taskRepository.findAll();
		assertThat(tasks).hasSize(2); // Only valid rows imported
	}

	@Test
	void uploadCsvFile_ShouldReturnBadRequest_WhenEmptyFile() {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource("".getBytes()) {
			@Override
			public String getFilename() {
				return "empty.csv";
			}
		});

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		ResponseEntity<ApiResponse> response = restTemplate.exchange(createURLWithPort("/api/v1/tasks/upload"),
				HttpMethod.POST, requestEntity, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().isSuccess()).isFalse();
		assertThat(response.getBody().getMessage()).contains("File is empty");
	}

	@Test
	void uploadFile_ShouldReturnBadRequest_WhenUnsupportedFileType() {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource("some content".getBytes()) {
			@Override
			public String getFilename() {
				return "unsupported.txt";
			}
		});

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		ResponseEntity<ApiResponse> response = restTemplate.exchange(createURLWithPort("/api/v1/tasks/upload"),
				HttpMethod.POST, requestEntity, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().isSuccess()).isFalse();
		assertThat(response.getBody().getMessage()).contains("Unsupported file type");
	}

	@Test
	void uploadCsvFile_ShouldHandleLargeFile_WhenManyRows() throws Exception {
		StringBuilder csvContent = new StringBuilder("Task Name,Description,Status,Priority,Due Date\n");

		// Generate 100 rows
		for (int i = 1; i <= 100; i++) {
			csvContent.append("Task ").append(i).append(",Description ").append(i).append(",TODO,HIGH,2025-12-31\n");
		}

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(csvContent.toString().getBytes()) {
			@Override
			public String getFilename() {
				return "large_file.csv";
			}
		});

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		ResponseEntity<ApiResponse> response = restTemplate.exchange(createURLWithPort("/api/v1/tasks/upload"),
				HttpMethod.POST, requestEntity, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(taskRepository.count()).isEqualTo(100);
	}

	// ========== CONCURRENCY TESTS ==========

	@Test
	void concurrentTaskCreation_ShouldHandleMultipleRequests() throws Exception {
		int threadCount = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			final int taskNumber = i;
			executorService.submit(() -> {
				try {
					createTestTask("Concurrent Task " + taskNumber, "Description " + taskNumber);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await(10, TimeUnit.SECONDS);
		executorService.shutdown();

		assertThat(taskRepository.count()).isEqualTo(threadCount);
	}

	// ========== EDGE CASE TESTS ==========

	@Test
	void createTask_ShouldHandleSpecialCharacters_InTaskName() {
		TaskRequestDto taskRequest = new TaskRequestDto();
		taskRequest.setTaskName("Task with @#$%^&*()");
		taskRequest.setDescription("Description with special chars: !@#$%");
		taskRequest.setPriority(Priority.HIGH);
		taskRequest.setStatus(Status.TODO);
		taskRequest.setDueDate(LocalDate.of(2025, 12, 31));

		ResponseEntity<ApiResponse> response = restTemplate.postForEntity(createURLWithPort("/api/v1/tasks"),
				taskRequest, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	void createTask_ShouldHandleUnicodeCharacters() {
		TaskRequestDto taskRequest = new TaskRequestDto();
		taskRequest.setTaskName("Task with 中文 и русский");
		taskRequest.setDescription("Description with émojis 🚀");
		taskRequest.setPriority(Priority.HIGH);
		taskRequest.setStatus(Status.TODO);
		taskRequest.setDueDate(LocalDate.of(2025, 12, 31));

		ResponseEntity<ApiResponse> response = restTemplate.postForEntity(createURLWithPort("/api/v1/tasks"),
				taskRequest, ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	// ========== DATABASE TESTS ==========

	@Test
	@Sql(scripts = "/sql/insert-sample-tasks.sql")
	void getTasks_ShouldReturnPreloadedTasks_WhenDataExistsInDatabase() {
		ResponseEntity<ApiResponse> response = restTemplate.getForEntity(createURLWithPort("/api/v1/tasks"),
				ApiResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) response.getBody().getData();
		assertThat(tasks).isNotEmpty();
	}

	// ========== HELPER METHODS ==========

	private Long createTestTask(String taskName, String description) {
		TaskRequestDto taskRequest = new TaskRequestDto();
		taskRequest.setTaskName(taskName);
		taskRequest.setDescription(description);
		taskRequest.setStatus(Status.TODO);
		taskRequest.setPriority(Priority.HIGH);
		taskRequest.setDueDate(LocalDate.of(2025, 12, 31));

		ResponseEntity<ApiResponse> response = restTemplate.postForEntity(createURLWithPort("/api/v1/tasks"),
				taskRequest, ApiResponse.class);

		Map<String, Object> responseData = (Map<String, Object>) response.getBody().getData();
		return Long.valueOf(responseData.get("id").toString());
	}
}

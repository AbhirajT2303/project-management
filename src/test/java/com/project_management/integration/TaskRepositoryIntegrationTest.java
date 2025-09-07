//package com.project_management.integration;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//
//import com.project_management.entities.Priority;
//import com.project_management.entities.Status;
//import com.project_management.entities.Task;
//import com.project_management.repository.TaskRepository;
//
//@DataJpaTest
//class TaskRepositoryIntegrationTest {
//
//	@Autowired
//	private TestEntityManager entityManager;
//
//	@Autowired
//	private TaskRepository taskRepository;
//
//	@Test
//	void findById_ShouldReturnTask_WhenTaskExists() {
//		// Arrange
//		Task task = new Task();
//		task.setTaskName("Test Task");
//		task.setDescription("Test Description");
//		task.setStatus(Status.IN_PROGRESS);
//		task.setPriority(Priority.MEDIUM);
//		task.setDueDate(LocalDate.of(2025, 12, 31));
//
//		Task savedTask = entityManager.persistAndFlush(task);
//
//		// Act
//		Optional<Task> foundTask = taskRepository.findById(savedTask.getId());
//
//		// Assert
//		assertThat(foundTask).isPresent();
//		assertThat(foundTask.get().getTaskName()).isEqualTo("Test Task");
//	}
//
//	@Test
//	void findAll_ShouldReturnAllTasks() {
//		// Arrange
//		Task task1 = createTask("Task 1", "Description 1");
//		Task task2 = createTask("Task 2", "Description 2");
//
//		entityManager.persistAndFlush(task1);
//		entityManager.persistAndFlush(task2);
//
//		// Act
//		List<Task> tasks = taskRepository.findAll();
//
//		// Assert
//		assertThat(tasks).hasSize(2);
//		assertThat(tasks).extracting(Task::getTaskName).containsExactlyInAnyOrder("Task 1", "Task 2");
//	}
//
//	@Test
//	void save_ShouldPersistTask() {
//		// Arrange
//		Task task = createTask("New Task", "New Description");
//
//		// Act
//		Task savedTask = taskRepository.save(task);
//
//		// Assert
//		assertThat(savedTask.getId()).isNotNull();
//		assertThat(entityManager.find(Task.class, savedTask.getId())).isNotNull();
//	}
//
//	@Test
//	void deleteById_ShouldRemoveTask() {
//		// Arrange
//		Task task = createTask("Task to Delete", "Description");
//		Task savedTask = entityManager.persistAndFlush(task);
//		Long taskId = savedTask.getId();
//
//		// Act
//		taskRepository.deleteById(taskId);
//		entityManager.flush();
//
//		// Assert
//		assertThat(entityManager.find(Task.class, taskId)).isNull();
//	}
//
//	private Task createTask(String name, String description) {
//		Task task = new Task();
//		task.setTaskName(name);
//		task.setDescription(description);
//		task.setStatus(Status.IN_PROGRESS);
//		task.setPriority(Priority.MEDIUM);
//		task.setDueDate(LocalDate.of(2025, 12, 31));
//		return task;
//	}
//}

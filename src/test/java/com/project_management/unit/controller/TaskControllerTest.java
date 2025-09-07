//package com.project_management.unit.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//
//import java.time.LocalDate;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.project_management.controller.TaskController;
//import com.project_management.dto.TaskRequestDto;
//import com.project_management.dto.TaskResponseDto;
//import com.project_management.entities.Priority;
//import com.project_management.entities.Status;
//import com.project_management.exception.ResourceNotFoundException;
//import com.project_management.service.TaskService;
//
//@WebMvcTest(TaskController.class)
//class TaskControllerTest {
//
//	@Autowired
//	private MockMvc mockMvc;
//
//	@MockBean
//	private TaskService taskService;
//
//	@Autowired
//	private ObjectMapper objectMapper;
//
//	@Test
//	void createTask_ShouldReturn201_WhenValidInput() throws Exception {
//		TaskRequestDto dto = new TaskRequestDto();
//		dto.setTaskName("Test Task");
//		dto.setDescription("Test Description");
////		dto.setStatus(Status.ASSIGNED);
//		dto.setPriority(Priority.HIGH);
//		dto.setDueDate(LocalDate.now().plusDays(5));
//
//		TaskResponseDto responseDto = new TaskResponseDto();
//		responseDto.setTaskName("Test Task");
//
//		when(taskService.createTask(any(TaskRequestDto.class))).thenReturn(responseDto);
//
//		mockMvc.perform(post("/api/v1/tasks").contentType(MediaType.APPLICATION_JSON)
//				.content(objectMapper.writeValueAsString(dto))).andExpect(status().isCreated())
//				.andExpect(jsonPath("$.data.taskName").value("Test Task"));
//
//		verify(taskService).createTask(any(TaskRequestDto.class));
//	}
//
//	@Test
//	void getTask_ShouldReturn404_WhenTaskNotFound() throws Exception {
//		when(taskService.getTaskById(999L)).thenThrow(new ResourceNotFoundException("Task not found"));
//
//		mockMvc.perform(get("/api/v1/tasks/999")).andExpect(status().isNotFound());
//	}
//}

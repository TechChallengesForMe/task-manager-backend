package com.example.taskmanager;

import com.example.taskmanager.controller.TaskController;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskManagerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    private TaskRequest validTaskRequest;
    private TaskResponse validTaskResponse;
    private Task validTask;

    @BeforeEach
    void setUp() {
        // Setup test data
        validTaskRequest = new TaskRequest();
        validTaskRequest.setTitle("Complete HMCTS Report");
        validTaskRequest.setDescription("Finish the quarterly casework report");
        validTaskRequest.setStatus("pending");
        validTaskRequest.setDueDate(LocalDateTime.now().plusDays(7));

        validTaskResponse = new TaskResponse();
        validTaskResponse.setId(1L);
        validTaskResponse.setTitle("Complete HMCTS Report");
        validTaskResponse.setDescription("Finish the quarterly casework report");
        validTaskResponse.setStatus("pending");
        validTaskResponse.setDueDate(LocalDateTime.now().plusDays(7));
        validTaskResponse.setCreatedAt(LocalDateTime.now());
        validTaskResponse.setUpdatedAt(LocalDateTime.now());

        validTask = new Task();
        validTask.setId(1L);
        validTask.setTitle("Complete HMCTS Report");
        validTask.setDescription("Finish the quarterly casework report");
        validTask.setStatus("pending");
        validTask.setDueDate(LocalDateTime.now().plusDays(7));
        validTask.setCreatedAt(LocalDateTime.now());
        validTask.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== CONTROLLER TESTS ====================

    @Test
    void contextLoads() {
        // Test that the test context loads
        assertNotNull(mockMvc);
        assertNotNull(taskService);
        assertNotNull(taskRepository);
    }

    @Test
    void createTask_ShouldReturnCreatedTask() throws Exception {
        // Mock the service
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(validTaskResponse);

        // Perform POST request
        MvcResult result = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Complete HMCTS Report"))
                .andExpect(jsonPath("$.status").value("pending"))
                .andReturn();

        // Verify service was called
        verify(taskService, times(1)).createTask(any(TaskRequest.class));
    }

    @Test
    void createTask_WithEmptyTitle_ShouldReturnBadRequest() throws Exception {
        // Arrange
        validTaskRequest.setTitle("");

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_WithInvalidStatus_ShouldReturnBadRequest() throws Exception {
        // Arrange
        validTaskRequest.setStatus("invalid_status");

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_WithMissingDueDate_ShouldReturnBadRequest() throws Exception {
        // Arrange
        validTaskRequest.setDueDate(null);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllTasks_ShouldReturnListOfTasks() throws Exception {
        // Arrange
        when(taskService.getAllTasks()).thenReturn(Arrays.asList(validTaskResponse));

        // Act & Assert
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Complete HMCTS Report"))
                .andExpect(jsonPath("$[0].status").value("pending"));
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenTaskExists() throws Exception {
        // Arrange
        when(taskService.getTaskById(1L)).thenReturn(validTaskResponse);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Complete HMCTS Report"));
    }

    @Test
    void getTaskById_ShouldReturnNotFound_WhenTaskDoesNotExist() throws Exception {
        // Arrange
        when(taskService.getTaskById(999L)).thenThrow(new RuntimeException("Task not found"));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskStatus_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        validTaskResponse.setStatus("completed");
        when(taskService.updateTaskStatus(eq(1L), any(String.class))).thenReturn(validTaskResponse);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"completed\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    void updateFullTask_ShouldUpdateAllFields() throws Exception {
        // Arrange
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setStatus("in-progress");
        updateRequest.setDueDate(LocalDateTime.now().plusDays(14));

        validTaskResponse.setTitle("Updated Title");
        validTaskResponse.setDescription("Updated Description");
        validTaskResponse.setStatus("in-progress");
        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(validTaskResponse);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("in-progress"));
    }

    @Test
    void deleteTask_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        doNothing().when(taskService).deleteTask(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
        
        verify(taskService, times(1)).deleteTask(1L);
    }

    // ==================== SERVICE LAYER TESTS ====================

    @Test
    void serviceCreateTask_ShouldReturnTaskResponse() {
        // This tests the service logic (you can add more service tests)
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(validTaskResponse);
        
        TaskResponse response = taskService.createTask(validTaskRequest);
        
        assertNotNull(response);
        assertEquals("Complete HMCTS Report", response.getTitle());
    }

    // ==================== REPOSITORY LAYER TESTS (Integration) ====================

    @Test
    void taskRepository_SaveAndFind_ShouldWork() {
        // Mock repository behavior
        when(taskRepository.save(any(Task.class))).thenReturn(validTask);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(validTask));
        
        Task savedTask = taskRepository.save(validTask);
        Optional<Task> foundTask = taskRepository.findById(1L);
        
        assertNotNull(savedTask);
        assertTrue(foundTask.isPresent());
        assertEquals("Complete HMCTS Report", foundTask.get().getTitle());
    }

    @Test
    void taskRepository_Delete_ShouldRemoveTask() {
        // Mock repository behavior
        doNothing().when(taskRepository).deleteById(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        
        taskRepository.deleteById(1L);
        Optional<Task> foundTask = taskRepository.findById(1L);
        
        assertFalse(foundTask.isPresent());
    }
}
package com.visiplus.pmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visiplus.pmt.dto.AssigneeDTO;
import com.visiplus.pmt.dto.TaskResponseDTO;
import com.visiplus.pmt.entity.Task;
import com.visiplus.pmt.entity.TaskHistory;
import com.visiplus.pmt.enums.TaskStatus;
import com.visiplus.pmt.repository.TaskHistoryRepository;
import com.visiplus.pmt.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @InjectMocks
    private TaskController taskController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    void createTask_CreatesTask_WhenValidInput() throws Exception {
        Long projectId = 1L;
        Long userId = 2L;
        Task task = new Task();
        task.setName("Sample Task");

        TaskResponseDTO createdTask = new TaskResponseDTO();
        createdTask.setName("Sample Task");

        when(taskService.createTask(any(Task.class), eq(projectId), eq(userId))).thenReturn(createdTask);

        mockMvc.perform(post("/projects/" + projectId + "/tasks/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sample Task"));
    }

    @Test
    void assignTaskToMember_AssignsTask_WhenValidInput() throws Exception {
        Long projectId = 1L;
        Long taskId = 1L;
        Long userId = 2L;
        Long assigneeId = 3L;

        AssigneeDTO assigneeDTO = new AssigneeDTO(assigneeId, "AssigneeName", "assignee@example.com");
        TaskResponseDTO updatedTask = new TaskResponseDTO();
        updatedTask.setAssignee(assigneeDTO);

        when(taskService.assignTaskToMember(eq(taskId), eq(projectId), eq(assigneeId), eq(userId))).thenReturn(updatedTask);

        mockMvc.perform(post("/projects/" + projectId + "/tasks/" + taskId + "/assign-task/" + userId + "/" + assigneeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee.id").value(assigneeId))
                .andExpect(jsonPath("$.assignee.username").value("AssigneeName"))
                .andExpect(jsonPath("$.assignee.email").value("assignee@example.com"));
    }

    @Test
    void updateTask_UpdatesTask_WhenValidInput() throws Exception {
        Long projectId = 1L;
        Long taskId = 1L;
        Long userId = 2L;
        Task updatedTaskInfo = new Task();
        updatedTaskInfo.setName("Updated Task");

        TaskResponseDTO updatedTaskDTO = new TaskResponseDTO();
        updatedTaskDTO.setName("Updated Task");

        when(taskService.updateTask(eq(taskId), eq(projectId), eq(userId), any(Task.class))).thenReturn(updatedTaskDTO);

        mockMvc.perform(put("/projects/" + projectId + "/tasks/" + taskId + "/update/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Task"));
    }

    @Test
    void getTaskById_ReturnsTask_WhenValidInput() throws Exception {
        Long projectId = 1L;
        Long taskId = 1L;
        Long userId = 2L;

        TaskResponseDTO taskResponse = new TaskResponseDTO();
        taskResponse.setName("Sample Task");

        when(taskService.getTaskById(taskId, projectId, userId)).thenReturn(taskResponse);

        mockMvc.perform(get("/projects/" + projectId + "/tasks/" + taskId + "/view/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sample Task"));
    }

    @Test
    void getTasksByProjectId_ReturnsTasks_WhenValidInput() throws Exception {
        Long projectId = 1L;

        TaskResponseDTO task = new TaskResponseDTO();
        task.setName("Sample Task");

        List<TaskResponseDTO> tasks = List.of(task);
        when(taskService.getTasksByProjectId(projectId)).thenReturn(tasks);

        mockMvc.perform(get("/projects/" + projectId + "/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sample Task"));
    }

    @Test
    void getTasksByUserId_ReturnsTasks_WhenValidInput() throws Exception {
        Long userId = 1L;

        TaskResponseDTO task = new TaskResponseDTO();
        task.setName("Sample Task");

        List<TaskResponseDTO> tasks = List.of(task);
        when(taskService.getTasksByUserId(userId)).thenReturn(tasks);

        mockMvc.perform(get("/projects/tasks/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sample Task"));
    }

    @Test
    void getTasksByStatus_ReturnsTasks_WhenValidStatus() throws Exception {
        Long projectId = 1L;
        TaskStatus status = TaskStatus.TODO;

        TaskResponseDTO task = new TaskResponseDTO();
        task.setName("Sample Task");

        List<TaskResponseDTO> tasks = List.of(task);
        when(taskService.getTasksByStatus(status, projectId)).thenReturn(tasks);

        mockMvc.perform(get("/projects/" + projectId + "/tasks/status/" + status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sample Task"));
    }

    @Test
    void updateTaskStatus_UpdatesStatus_WhenValidInput() throws Exception {
        Long projectId = 1L;
        Long taskId = 1L;
        Long userId = 2L;

        Map<String, String> statusBody = Collections.singletonMap("status", "IN_PROGRESS");
        TaskResponseDTO updatedTask = new TaskResponseDTO();
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);

        when(taskService.updateTaskStatus(taskId, projectId, userId, "IN_PROGRESS")).thenReturn(updatedTask);

        mockMvc.perform(put("/projects/" + projectId + "/tasks/" + taskId + "/update-status/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getTaskHistory_ReturnsHistory_WhenHistoryExists() throws Exception {
        Long projectId = 1L;
        Long taskId = 1L;

        List<TaskHistory> historyList = List.of(new TaskHistory());

        when(taskHistoryRepository.findByTaskId(taskId)).thenReturn(historyList);

        mockMvc.perform(get("/projects/" + projectId + "/tasks/" + taskId + "/history"))
                .andExpect(status().isOk());
    }

    @Test
    void getTaskHistory_ReturnsNotFound_WhenNoHistoryExists() throws Exception {
        Long projectId = 1L;
        Long taskId = 1L;

        when(taskHistoryRepository.findByTaskId(taskId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/projects/" + projectId + "/tasks/" + taskId + "/history"))
                .andExpect(status().isNotFound());
    }
}

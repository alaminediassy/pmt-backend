package com.visiplus.pmt.controller;

import com.visiplus.pmt.dto.TaskResponseDTO;
import com.visiplus.pmt.entity.Task;
import com.visiplus.pmt.entity.TaskHistory;
import com.visiplus.pmt.enums.TaskStatus;
import com.visiplus.pmt.repository.TaskHistoryRepository;
import com.visiplus.pmt.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/projects")
public class TaskController {

    private final TaskService taskService;
    private final TaskHistoryRepository taskHistoryRepository;

    public TaskController(TaskService taskService, TaskHistoryRepository taskHistoryRepository) {
        this.taskService = taskService;
        this.taskHistoryRepository = taskHistoryRepository;
    }

    // Endpoint to create task
    @PostMapping("/{projectId}/tasks/{userId}")
    public ResponseEntity<TaskResponseDTO> createTask(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody Task task) {
        TaskResponseDTO createdTask = taskService.createTask(task, projectId, userId);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // Endpoint to assign task to member
    @PostMapping("/{projectId}/tasks/{taskId}/assign-task/{userId}/{assigneeId}")
    public ResponseEntity<TaskResponseDTO> assignTaskToMember(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long assigneeId,
            @PathVariable Long userId) {
        TaskResponseDTO updatedTask = taskService.assignTaskToMember(taskId, projectId, assigneeId, userId);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    // Endpoint to update task
    @PutMapping("/{projectId}/tasks/{taskId}/update/{userId}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long userId,
            @RequestBody Task updateTask) {
        TaskResponseDTO updatedTaskDTO = taskService.updateTask(taskId, projectId, userId, updateTask);
        return new ResponseEntity<>(updatedTaskDTO, HttpStatus.OK);
    }

    // Get task by id
    @GetMapping("/{projectId}/tasks/{taskId}/view/{userId}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        TaskResponseDTO taskResponse = taskService.getTaskById(taskId, projectId, userId);
        return new ResponseEntity<>(taskResponse, HttpStatus.OK);
    }

    // get task by status
    @GetMapping("/{projectId}/tasks/status/{status}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByStatus(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status) {
        List<TaskResponseDTO> tasks = taskService.getTasksByStatus(status, projectId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // Update task status
    @PutMapping("/{projectId}/tasks/{taskId}/update-status/{userId}")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long userId,
            @RequestBody Map<String, String> statusBody) {
        String status = statusBody.get("status");
        TaskResponseDTO updatedTask = taskService.updateTaskStatus(taskId, projectId, userId, status);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    // Get task history
    @GetMapping("/{projectId}/tasks/{taskId}/history")
    public ResponseEntity<List<TaskHistory>> getTaskHistory(@PathVariable Long projectId, @PathVariable Long taskId) {
        List<TaskHistory> historyList = taskHistoryRepository.findByTaskId(taskId);

        if (historyList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(historyList);
        }
        return ResponseEntity.ok(historyList);
    }
}


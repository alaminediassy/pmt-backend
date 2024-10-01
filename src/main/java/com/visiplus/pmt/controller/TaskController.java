package com.visiplus.pmt.controller;

import com.visiplus.pmt.dto.TaskResponseDTO;
import com.visiplus.pmt.entity.Task;
import com.visiplus.pmt.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/{projectId}/tasks/{userId}")
    public ResponseEntity<TaskResponseDTO> createTask(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody Task task) {
        TaskResponseDTO createdTask = taskService.createTask(task, projectId, userId);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PostMapping("/{projectId}/tasks/{taskId}/assign/{userId}/{assigneeId}")
    public ResponseEntity<TaskResponseDTO> assignTaskToMember(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long assigneeId,
            @PathVariable Long userId) {
        TaskResponseDTO updatedTask = taskService.assignTaskToMember(taskId, projectId, assigneeId, userId);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

}


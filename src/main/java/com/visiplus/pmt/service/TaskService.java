package com.visiplus.pmt.service;

import com.visiplus.pmt.dto.TaskResponseDTO;
import com.visiplus.pmt.entity.Task;
import com.visiplus.pmt.enums.TaskStatus;
import jakarta.transaction.Transactional;

import java.util.List;

public interface TaskService {
    TaskResponseDTO createTask(Task task, Long projectId, Long userId);
    TaskResponseDTO assignTaskToMember(Long taskId, Long projectId, Long assigneeId, Long userId);
    TaskResponseDTO updateTask(Long taskId, Long projectId, Long userId, Task updateTask);
    TaskResponseDTO getTaskById(Long taskId, Long projectId, Long userId);
    List<TaskResponseDTO> getTasksByStatus(TaskStatus status, Long projectId);
    TaskResponseDTO updateTaskStatus(Long taskId, Long projectId, Long userId, String status);
}

package com.visiplus.pmt.service;

import com.visiplus.pmt.dto.TaskResponseDTO;
import com.visiplus.pmt.entity.Task;
import jakarta.transaction.Transactional;

public interface TaskService {
    TaskResponseDTO createTask(Task task, Long projectId, Long userId);
    TaskResponseDTO assignTaskToMember(Long taskId, Long projectId, Long assigneeId, Long userId);
}

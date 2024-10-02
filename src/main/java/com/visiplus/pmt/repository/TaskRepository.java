package com.visiplus.pmt.repository;

import com.visiplus.pmt.entity.Task;
import com.visiplus.pmt.enums.TaskStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskRepository extends CrudRepository<Task, Long> {
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
}

package com.visiplus.pmt.repository;

import com.visiplus.pmt.entity.TaskHistory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskHistoryRepository extends CrudRepository<TaskHistory, Long> {
    List<TaskHistory> findByTaskId(Long taskId);
}

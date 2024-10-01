package com.visiplus.pmt.repository;

import com.visiplus.pmt.entity.Task;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task, Long> {
}

package com.visiplus.pmt.dto;

import com.visiplus.pmt.enums.Priority;
import com.visiplus.pmt.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskResponseDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate dueDate;
    private Priority priority;
    private LocalDate completionDate;
    private TaskStatus status;
    private ProjectSimpleDTO project;
    private AssigneeDTO assignee;
}

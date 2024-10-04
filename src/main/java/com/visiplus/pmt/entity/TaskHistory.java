package com.visiplus.pmt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;

    private Long changedBy;

    private String fieldName;

    private String oldValue;

    private String newValue;

    private LocalDateTime changedAt;
}

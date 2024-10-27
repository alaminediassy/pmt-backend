package com.visiplus.pmt.entity;

import com.visiplus.pmt.enums.Priority;
import com.visiplus.pmt.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = true)
    private AppUser assignee;

    private LocalDate completionDate;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    public Task(Object o, String updatedTaskName, String updatedDescription, LocalDate localDate, Priority priority, Object o1, Object o2, TaskStatus taskStatus) {
    }
}

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

    private Long taskId; // ID de la tâche

    private Long changedBy; // ID de l'utilisateur qui a fait le changement

    private String fieldName; // Nom du champ modifié

    private String oldValue; // Ancienne valeur

    private String newValue; // Nouvelle valeur

    private LocalDateTime changedAt;
}

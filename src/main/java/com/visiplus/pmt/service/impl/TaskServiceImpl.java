package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.dto.AssigneeDTO;
import com.visiplus.pmt.dto.ProjectSimpleDTO;
import com.visiplus.pmt.dto.TaskResponseDTO;
import com.visiplus.pmt.entity.*;
import com.visiplus.pmt.enums.Role;
import com.visiplus.pmt.enums.TaskStatus;
import com.visiplus.pmt.repository.*;
import com.visiplus.pmt.service.EmailService;
import com.visiplus.pmt.service.TaskService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;
    private final TaskHistoryRepository taskHistoryRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           ProjectMemberRoleRepository projectMemberRoleRepository, AppUserRepository appUserRepository, EmailService emailService, TaskHistoryRepository taskHistoryRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.appUserRepository = appUserRepository;
        this.emailService = emailService;
        this.taskHistoryRepository = taskHistoryRepository;
    }

    @Override
    @Transactional
    public TaskResponseDTO createTask(Task task, Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        ProjectMemberRole memberRole = projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"));

        if (memberRole.getRole() != Role.ADMIN && memberRole.getRole() != Role.MEMBER) {
            throw new RuntimeException("You do not have permission to create tasks in this project");
        }

        task.setStatus(TaskStatus.TODO);
        task.setProject(project);

        Task savedTask = taskRepository.save(task);

        return getTaskResponseDTO(savedTask, project);
    }

    private static TaskResponseDTO getTaskResponseDTO(Task savedTask, Project project) {
        TaskResponseDTO taskResponseDTO = new TaskResponseDTO();
        taskResponseDTO.setId(savedTask.getId());
        taskResponseDTO.setName(savedTask.getName());
        taskResponseDTO.setDescription(savedTask.getDescription());
        taskResponseDTO.setDueDate(savedTask.getDueDate());
        taskResponseDTO.setCompletionDate(savedTask.getCompletionDate());
        taskResponseDTO.setPriority(savedTask.getPriority());
        taskResponseDTO.setStatus(savedTask.getStatus());

        ProjectSimpleDTO projectSimpleDTO = new ProjectSimpleDTO();
        projectSimpleDTO.setId(project.getId());
        projectSimpleDTO.setName(project.getName());
        projectSimpleDTO.setDescription(project.getDescription());
        taskResponseDTO.setProject(projectSimpleDTO);

        if (savedTask.getAssignee() != null) {
            AppUser assignee = savedTask.getAssignee();
            taskResponseDTO.setAssignee(new AssigneeDTO(assignee.getId(), assignee.getUsername(), assignee.getEmail()));
        }

        return taskResponseDTO;
    }


    @Override
    @Transactional
    public TaskResponseDTO assignTaskToMember(Long taskId, Long projectId, Long assigneeId, Long userId) {
        ProjectMemberRole memberRole = projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"));

        if (memberRole.getRole() != Role.ADMIN && memberRole.getRole() != Role.MEMBER) {
            throw new RuntimeException("You do not have permission to assign tasks in this project");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        AppUser assignee = appUserRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("Assignee not found with id: " + assigneeId));

        task.setAssignee(assignee);

        Task updatedTask = taskRepository.save(task);

        // Call Method to send email
        sendTaskAssignmentEmail(task, assignee);

        return getTaskResponseDTO(updatedTask, task.getProject());
    }

    // Méthod to send email
    private void sendTaskAssignmentEmail(Task task, AppUser assignee) {
        String subject = "New Task Assigned: " + task.getName();
        String body = String.format(
                "Bonjour %s,\n\n" +
                        "Vous avez été affecté à la tache : %s\n" +
                        "Description: %s\n" +
                        "Date d'échéance : %s\n\n" +
                        "Cordialement,\n" +
                        "L'équipe de Gestion de Projet",
                assignee.getUsername(),
                task.getName(),
                task.getDescription(),
                task.getDueDate()
        );
        emailService.sendTaskAssignmentEmail(assignee.getEmail(), subject, body);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(Long taskId, Long projectId, Long userId, Task updatedTaskInfo) {
        // Vérifier les permissions de l'utilisateur
        ProjectMemberRole memberRole = projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"));
        if (memberRole.getRole() != Role.ADMIN && memberRole.getRole() != Role.MEMBER) {
            throw new RuntimeException("You do not have permission to update tasks in this project");
        }

        // Récupérer la tâche
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Vérifier que la tâche appartient au bon projet
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        // Mise à jour des informations de la tâche
        if (!task.getName().equals(updatedTaskInfo.getName())) {
            saveTaskHistory(taskId, userId, "name", task.getName(), updatedTaskInfo.getName());
            task.setName(updatedTaskInfo.getName());
        }
        // Ajouter d'autres champs comme pour la description, dueDate, etc.

        // Mise à jour du statut
        if (task.getStatus() != updatedTaskInfo.getStatus()) {
            saveTaskHistory(taskId, userId, "status", task.getStatus().name(), updatedTaskInfo.getStatus().name());
            task.setStatus(updatedTaskInfo.getStatus());
        }

        // Sauvegarder les modifications
        Task updatedTask = taskRepository.save(task);
        return getTaskResponseDTO(updatedTask, task.getProject());
    }

    // Méthode pour enregistrer l'historique des changements
    private void saveTaskHistory(Long taskId, Long userId, String fieldName, String oldValue, String newValue) {
        TaskHistory taskHistory = new TaskHistory();
        taskHistory.setTaskId(taskId);
        taskHistory.setChangedBy(userId);
        taskHistory.setFieldName(fieldName);
        taskHistory.setOldValue(oldValue);
        taskHistory.setNewValue(newValue);
        taskHistory.setChangedAt(LocalDateTime.now());
        taskHistoryRepository.save(taskHistory);
    }



    @Override
    public TaskResponseDTO getTaskById(Long taskId, Long projectId, Long userId) {
        ProjectMemberRole memberRole = projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"));

        // Retrieve task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Verify that the task belongs to the project
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }
        return getTaskResponseDTO(task, task.getProject());
    }

    @Override
    public List<TaskResponseDTO> getTasksByStatus(TaskStatus status, Long projectId) {
        List<Task> tasks = taskRepository.findByProjectIdAndStatus(projectId, status);
        return tasks.stream()
                .map(task -> getTaskResponseDTO(task, task.getProject()))
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponseDTO updateTaskStatus(Long taskId, Long projectId, Long userId, String status) {
        // Check if the user has the permissions
        ProjectMemberRole memberRole = projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"));

        if (memberRole.getRole() != Role.ADMIN && memberRole.getRole() != Role.MEMBER && memberRole.getRole() != Role.OBSERVER) {
            throw new RuntimeException("You do not have permission to update tasks in this project");
        }

        // Retrieve task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Verify that the task belongs to the project
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        // Update the status of the task
        task.setStatus(TaskStatus.valueOf(status));

        // Save changes
        Task updatedTask = taskRepository.save(task);

        // Return the updated DTO
        return getTaskResponseDTO(updatedTask, task.getProject());
    }


    @GetMapping("/{projectId}/tasks/{taskId}/history")
    public ResponseEntity<String> getTaskHistory(@PathVariable Long projectId, @PathVariable Long taskId) {
        List<TaskHistory> historyList = taskHistoryRepository.findByTaskId(taskId);

        if (historyList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No history found for the task with id: " + taskId);
        }

        return ResponseEntity.ok(historyList.toString());
    }

}

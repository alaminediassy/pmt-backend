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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TaskServiceImpl implements TaskService {

    // Dependencies injected for task, project, user, and email repositories
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;
    private final TaskHistoryRepository taskHistoryRepository;

    // Constructor-based dependency injection
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

    /**
     * Creates a new task for a project and associates it with a user.
     *
     * @param task the task to be created
     * @param projectId the ID of the project
     * @param userId the ID of the user creating the task
     * @return TaskResponseDTO containing the task and project details
     * @throws RuntimeException if the project or user is not valid
     */
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

        // Set task status to TODO and associate with the project
        task.setStatus(TaskStatus.TODO);
        task.setProject(project);

        // Save the task to the repository
        Task savedTask = taskRepository.save(task);

        return getTaskResponseDTO(savedTask, project);
    }

    /**
     * Helper method to convert Task and Project entities to a TaskResponseDTO.
     */
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

    /**
     * Assigns a task to a project member.
     *
     * @param taskId the ID of the task
     * @param projectId the ID of the project
     * @param assigneeId the ID of the assignee
     * @param userId the ID of the user assigning the task
     * @return TaskResponseDTO with updated task details
     * @throws RuntimeException if the task, project, or user is invalid
     */
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

        // Save the updated task
        Task updatedTask = taskRepository.save(task);

        // Send email notification to assignee
        sendTaskAssignmentEmail(task, assignee);

        return getTaskResponseDTO(updatedTask, task.getProject());
    }

    /**
     * Method to send an email notification for task assignment.
     *
     * @param task the task assigned
     * @param assignee the user assigned to the task
     */
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

    /**
     * Updates task details and logs changes in task history.
     *
     * @param taskId the ID of the task
     * @param projectId the ID of the project
     * @param userId the ID of the user updating the task
     * @param updatedTaskInfo the updated task details
     * @return TaskResponseDTO with updated task information
     * @throws RuntimeException if task, project, or user is invalid
     */
    @Override
    @Transactional
    public TaskResponseDTO updateTask(Long taskId, Long projectId, Long userId, Task updatedTaskInfo) {
        ProjectMemberRole memberRole = projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"));

        if (memberRole.getRole() != Role.ADMIN && memberRole.getRole() != Role.MEMBER) {
            throw new RuntimeException("You do not have permission to update tasks in this project");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        if (!task.getName().equals(updatedTaskInfo.getName())) {
            saveTaskHistory(taskId, userId, "name", task.getName(), updatedTaskInfo.getName());
            task.setName(updatedTaskInfo.getName());
        }

        if (!task.getDescription().equals(updatedTaskInfo.getDescription())) {
            saveTaskHistory(taskId, userId, "description", task.getDescription(), updatedTaskInfo.getDescription());
            task.setDescription(updatedTaskInfo.getDescription());
        }

        if (updatedTaskInfo.getDueDate() != null && !task.getDueDate().equals(updatedTaskInfo.getDueDate())) {
            saveTaskHistory(taskId, userId, "dueDate", task.getDueDate().toString(), updatedTaskInfo.getDueDate().toString());
            task.setDueDate(updatedTaskInfo.getDueDate());
        }

        if (updatedTaskInfo.getCompletionDate() != null) {
            if (task.getCompletionDate() == null || !task.getCompletionDate().equals(updatedTaskInfo.getCompletionDate())) {
                saveTaskHistory(
                        taskId,
                        userId,
                        "completionDate",
                        task.getCompletionDate() != null ? task.getCompletionDate().toString() : "null",
                        updatedTaskInfo.getCompletionDate().toString()
                );
                task.setCompletionDate(updatedTaskInfo.getCompletionDate());
            }
        } else {
            if (task.getCompletionDate() != null) {
                saveTaskHistory(taskId, userId, "completionDate", task.getCompletionDate().toString(), "null");
                task.setCompletionDate(null);
            }
        }

        if (!task.getPriority().equals(updatedTaskInfo.getPriority())) {
            saveTaskHistory(taskId, userId, "priority", task.getPriority().name(), updatedTaskInfo.getPriority().name());
            task.setPriority(updatedTaskInfo.getPriority());
        }

        if (task.getStatus() != updatedTaskInfo.getStatus()) {
            saveTaskHistory(taskId, userId, "status", task.getStatus().name(), updatedTaskInfo.getStatus().name());
            task.setStatus(updatedTaskInfo.getStatus());
        }

        Task updatedTask = taskRepository.save(task);

        return getTaskResponseDTO(updatedTask, task.getProject());
    }


    /**
     * Logs changes made to a task's field in the task history.
     *
     * @param taskId the ID of the task
     * @param userId the ID of the user making changes
     * @param fieldName the field that was changed
     * @param oldValue the old value of the field
     * @param newValue the new value of the field
     */
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


    /**
     * Retrieves a task by its ID.
     *
     * @param taskId the task ID
     * @param projectId the project ID
     * @param userId the user ID
     * @return TaskResponseDTO with task details
     * @throws RuntimeException if the task or project is invalid
     */
    @Override
    public TaskResponseDTO getTaskById(Long taskId, Long projectId, Long userId) {
        ProjectMemberRole memberRole = projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }
        return getTaskResponseDTO(task, task.getProject());
    }

    /**
     * Retrieves all tasks in a project by status.
     *
     * @param status the status of the tasks
     * @param projectId the project ID
     * @return List of TaskResponseDTO containing the task details
     */
    @Override
    public List<TaskResponseDTO> getTasksByStatus(TaskStatus status, Long projectId) {
        List<Task> tasks = taskRepository.findByProjectIdAndStatus(projectId, status);
        return tasks.stream()
                .map(task -> getTaskResponseDTO(task, task.getProject()))
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a task.
     *
     * @param taskId the task ID
     * @param projectId the project ID
     * @param userId the user ID
     * @param status the new task status
     * @return TaskResponseDTO with updated task details
     */
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

    @Override
    public List<TaskResponseDTO> getTasksByProjectId(Long projectId) {
        return List.of();
    }



    @Override
    public List<TaskResponseDTO> getTasksByUserId(Long userId) {
        List<ProjectMemberRole> memberRoles = projectMemberRoleRepository.findByMemberId(userId);

        List<Task> tasks = memberRoles.stream()
                .flatMap(role -> {
                    List<Task> projectTasks = taskRepository.findByProjectId(role.getProject().getId());
                    return projectTasks.stream();
                })
                .toList();

        return tasks.stream().map(task -> getTaskResponseDTO(task, task.getProject())).collect(Collectors.toList());
    }


    /**
     * Retrieves task history for a given project and task.
     *
     * @param projectId the project ID
     * @param taskId the task ID
     * @return ResponseEntity with the task history or error message
     */
    @GetMapping("/{projectId}/tasks/{taskId}/history")
    public ResponseEntity<String> getTaskHistory(@PathVariable Long projectId, @PathVariable Long taskId) {
        List<TaskHistory> historyList = taskHistoryRepository.findByTaskId(taskId);

        if (historyList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No history found for the task with id: " + taskId);
        }

        return ResponseEntity.ok(historyList.toString());
    }

}

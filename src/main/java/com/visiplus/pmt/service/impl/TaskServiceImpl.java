package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.dto.ProjectSimpleDTO;
import com.visiplus.pmt.dto.TaskResponseDTO;
import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.entity.Project;
import com.visiplus.pmt.entity.ProjectMemberRole;
import com.visiplus.pmt.entity.Task;
import com.visiplus.pmt.enums.Role;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.repository.ProjectMemberRoleRepository;
import com.visiplus.pmt.repository.ProjectRepository;
import com.visiplus.pmt.repository.TaskRepository;
import com.visiplus.pmt.service.TaskService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final AppUserRepository appUserRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           ProjectMemberRoleRepository projectMemberRoleRepository, AppUserRepository appUserRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.appUserRepository = appUserRepository;
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
        taskResponseDTO.setPriority(savedTask.getPriority());

        // Associer un DTO simplifié pour le projet
        ProjectSimpleDTO projectSimpleDTO = new ProjectSimpleDTO();
        projectSimpleDTO.setId(project.getId());
        projectSimpleDTO.setName(project.getName());
        projectSimpleDTO.setDescription(project.getDescription());
        taskResponseDTO.setProject(projectSimpleDTO);
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

        return getTaskResponseDTO(updatedTask, task.getProject());
    }

}


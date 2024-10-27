package com.visiplus.pmt.service;

import com.visiplus.pmt.dto.TaskResponseDTO;
import com.visiplus.pmt.entity.*;
import com.visiplus.pmt.enums.Priority;
import com.visiplus.pmt.enums.Role;
import com.visiplus.pmt.enums.TaskStatus;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.repository.ProjectMemberRoleRepository;
import com.visiplus.pmt.repository.ProjectRepository;
import com.visiplus.pmt.repository.TaskHistoryRepository;
import com.visiplus.pmt.repository.TaskRepository;
import com.visiplus.pmt.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRoleRepository projectMemberRoleRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTask_CreatesTask_WhenValidDataProvided() {
        Long projectId = 1L;
        Long userId = 1L;

        // Create mock project and user
        Project project = new Project();
        project.setId(projectId);

        Task task = new Task(null, "Task Name", "Description", LocalDate.now(), Priority.HIGH, project, null, null, TaskStatus.TODO);

        // Simulate project retrieval
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Set up user with ADMIN role
        AppUser user = new AppUser();
        user.setId(userId);
        ProjectMemberRole memberRole = new ProjectMemberRole();
        memberRole.setProject(project);
        memberRole.setMember(user);
        memberRole.setRole(Role.ADMIN); // Explicitly assign ADMIN role

        // Ensure role is correctly assigned
        assertEquals(Role.ADMIN, memberRole.getRole(), "Role is not correctly assigned.");

        // Configure mock to return this role
        when(projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)).thenReturn(Optional.of(memberRole));

        // Simulate task saving
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(1L); // Assign a mock ID to the created task
            return savedTask;
        });

        // Execute task creation
        TaskResponseDTO createdTask = taskService.createTask(task, projectId, userId);

        // Verify task was created and saved
        assertEquals(task.getName(), createdTask.getName());
        assertEquals(TaskStatus.TODO, createdTask.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }


    @Test
    void updateTask_RecordsTaskHistory_WhenFieldsAreChanged() {
        Long projectId = 1L;
        Long taskId = 1L;
        Long userId = 1L;

        // Mock the project, task, and user with roles
        Project project = new Project();
        project.setId(projectId);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);
        task.setName("Original Name");
        task.setDescription("Original Description");
        task.setDueDate(LocalDate.now());
        task.setPriority(Priority.MEDIUM);
        task.setStatus(TaskStatus.TODO);

        // Updated task information
        Task updatedTaskInfo = new Task();
        updatedTaskInfo.setName("Updated Name");
        updatedTaskInfo.setDescription("Updated Description");
        updatedTaskInfo.setDueDate(LocalDate.now().plusDays(1));
        updatedTaskInfo.setPriority(Priority.HIGH);
        updatedTaskInfo.setStatus(TaskStatus.IN_PROGRESS);

        // Set up the user with ADMIN role
        AppUser user = new AppUser();
        user.setId(userId);
        ProjectMemberRole memberRole = new ProjectMemberRole();
        memberRole.setProject(project);
        memberRole.setMember(user);
        // Ensure the user has ADMIN role
        memberRole.setRole(Role.ADMIN);

        // Mock the repository calls
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)).thenReturn(Optional.of(memberRole));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Execute the update task method
        TaskResponseDTO updatedTaskDTO = taskService.updateTask(taskId, projectId, userId, updatedTaskInfo);

        // Verify that the task's fields were updated
        assertEquals(updatedTaskInfo.getName(), updatedTaskDTO.getName());
        assertEquals(updatedTaskInfo.getDescription(), updatedTaskDTO.getDescription());
        assertEquals(updatedTaskInfo.getDueDate(), updatedTaskDTO.getDueDate());
        assertEquals(updatedTaskInfo.getPriority(), updatedTaskDTO.getPriority());
        assertEquals(updatedTaskInfo.getStatus(), updatedTaskDTO.getStatus());

        // Verify that task history entries were saved for each updated field
        verify(taskHistoryRepository, atLeast(1)).save(any(TaskHistory.class));
    }


    @Test
    void assignTaskToMember_AssignsTask_WhenUserHasPermission() {
        Long projectId = 1L;
        Long taskId = 1L;
        Long userId = 1L;
        Long assigneeId = 2L;

        // Mock entities and roles
        Project project = new Project();
        project.setId(projectId);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);

        AppUser user = new AppUser();
        user.setId(userId);

        AppUser assignee = new AppUser();
        assignee.setId(assigneeId);

        ProjectMemberRole memberRole = new ProjectMemberRole();
        memberRole.setProject(project);
        memberRole.setMember(user);
        memberRole.setRole(Role.ADMIN); // Assign ADMIN role

        // Mock repository responses
        when(projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)).thenReturn(Optional.of(memberRole));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(appUserRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(task)).thenReturn(task);

        // Execute method
        TaskResponseDTO assignedTask = taskService.assignTaskToMember(taskId, projectId, assigneeId, userId);

        // Verify and assert
        assertEquals(assigneeId, assignedTask.getAssignee().getId());
    }

    @Test
    void updateTaskStatus_UpdatesStatus_WhenValidInput() {
        Long projectId = 1L;
        Long taskId = 1L;
        Long userId = 1L;

        // Mock entities and roles
        Project project = new Project();
        project.setId(projectId);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);

        AppUser user = new AppUser();
        user.setId(userId);

        ProjectMemberRole memberRole = new ProjectMemberRole();
        memberRole.setProject(project);
        memberRole.setMember(user);
        memberRole.setRole(Role.MEMBER); // Assign MEMBER role

        // Mock repository responses
        when(projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId)).thenReturn(Optional.of(memberRole));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        // Execute method
        TaskResponseDTO updatedTask = taskService.updateTaskStatus(taskId, projectId, userId, "IN_PROGRESS");

        // Assert status update
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }


    @Test
    void getTaskById_ReturnsTask_WhenValidInput() {
        Long taskId = 1L;
        Long projectId = 1L;
        Long userId = 1L;

        Project project = new Project();
        project.setId(projectId);

        Task task = new Task(taskId, "Task Name", "Description", LocalDate.now(), Priority.HIGH, project, null, null, TaskStatus.TODO);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, userId))
                .thenReturn(Optional.of(new ProjectMemberRole(project, new AppUser(), Role.ADMIN)));

        TaskResponseDTO response = taskService.getTaskById(taskId, projectId, userId);

        assertEquals(taskId, response.getId());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void getTasksByStatus_ReturnsTasks_WhenValidStatus() {
        Long projectId = 1L;
        TaskStatus status = TaskStatus.TODO;

        Project project = new Project();
        project.setId(projectId);

        Task task = new Task(1L, "Task Name", "Description", LocalDate.now(), Priority.HIGH, project, null, null, status);

        when(taskRepository.findByProjectIdAndStatus(projectId, status)).thenReturn(List.of(task));

        List<TaskResponseDTO> tasks = taskService.getTasksByStatus(status, projectId);

        assertEquals(1, tasks.size());
        assertEquals(status, tasks.get(0).getStatus());
    }
}

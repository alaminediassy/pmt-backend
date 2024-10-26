package com.visiplus.pmt.service;

import com.visiplus.pmt.dto.MemberDTO;
import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.entity.Project;
import com.visiplus.pmt.entity.ProjectMemberRole;
import com.visiplus.pmt.enums.Role;
import com.visiplus.pmt.exception.UserNotFoundException;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.repository.ProjectMemberRoleRepository;
import com.visiplus.pmt.repository.ProjectRepository;
import com.visiplus.pmt.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ProjectMemberRoleRepository projectMemberRoleRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProject_AssignsOwnerAndSavesProject() {
        Project project = new Project(null, "Test Project", "Description", null, null, null);
        AppUser appUser = new AppUser(1L, "owner", "owner@example.com", "password", null);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(projectRepository.save(project)).thenReturn(project);

        Project createdProject = projectService.createProject(project, 1L);

        assertEquals(appUser, createdProject.getOwner());
        verify(projectRepository, times(1)).save(project);
        verify(projectMemberRoleRepository, times(1)).save(any(ProjectMemberRole.class));
    }

    @Test
    void addMemberToProject_AddsMemberIfNotAlreadyInProject() {
        // Arrange
        Long projectId = 1L;
        String email = "newuser@example.com";

        Project project = new Project();
        project.setId(projectId);
        project.setMembersWithRoles(new HashSet<>());

        AppUser newUser = new AppUser();
        newUser.setId(2L);
        newUser.setEmail(email);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(newUser));
        when(projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, newUser.getId())).thenReturn(Optional.empty());

        // Act
        Project updatedProject = projectService.addMemberToProject(projectId, email);

        // Assert
        assertTrue(updatedProject.getMembersWithRoles().stream()
                .anyMatch(role -> role.getMember().equals(newUser) && role.getRole() == Role.MEMBER));
        verify(projectMemberRoleRepository, times(1)).save(any(ProjectMemberRole.class));
    }


    @Test
    void assignRoleToMember_UpdatesRole() {
        // Arrange
        Long projectId = 1L;
        Long memberId = 2L;
        Role newRole = Role.ADMIN;

        Project project = new Project();
        project.setId(projectId);

        AppUser member = new AppUser();
        member.setId(memberId);

        ProjectMemberRole memberRole = new ProjectMemberRole();
        memberRole.setProject(project);
        memberRole.setMember(member);
        memberRole.setRole(Role.MEMBER);

        when(projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, memberId)).thenReturn(Optional.of(memberRole));
        when(projectMemberRoleRepository.save(any(ProjectMemberRole.class))).thenReturn(memberRole);

        // Act
        ProjectMemberRole updatedRole = projectService.assignRoleToMember(projectId, memberId, newRole);

        // Assert
        assertEquals(newRole, updatedRole.getRole());
        verify(projectMemberRoleRepository, times(1)).save(memberRole);
    }

    @Test
    void getAllProjects_ReturnsListOfProjects() {
        List<Project> projects = List.of(new Project(), new Project());
        when(projectRepository.findAll()).thenReturn(projects);

        List<Project> result = projectService.getAllProjects();

        assertEquals(2, result.size());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void getProjectsByUserId_ReturnsProjectsForUser() {
        List<Project> projects = List.of(new Project());
        when(projectRepository.findProjectsByUserId(1L)).thenReturn(projects);

        List<Project> result = projectService.getProjectsByUserId(1L);

        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findProjectsByUserId(1L);
    }

    @Test
    void getProjectMembers_ReturnsMembersForProject() {
        ProjectMemberRole memberRole = new ProjectMemberRole();
        AppUser user = new AppUser(1L, "user", "user@example.com", "password", null);
        memberRole.setMember(user);
        when(projectMemberRoleRepository.findByProjectId(1L)).thenReturn(List.of(memberRole));

        List<MemberDTO> members = projectService.getProjectMembers(1L);

        assertEquals(1, members.size());
        assertEquals("user@example.com", members.get(0).getEmail());
        verify(projectMemberRoleRepository, times(1)).findByProjectId(1L);
    }
}

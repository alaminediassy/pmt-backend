package com.visiplus.pmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visiplus.pmt.dto.InviteRequestDTO;
import com.visiplus.pmt.dto.MemberDTO;
import com.visiplus.pmt.dto.RoleAssignmentDTO;
import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.entity.Project;
import com.visiplus.pmt.entity.ProjectMemberRole;
import com.visiplus.pmt.enums.Role;
import com.visiplus.pmt.exception.UserNotFoundException;
import com.visiplus.pmt.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProject_ReturnsCreatedProject_WhenValidInput() throws Exception {
        Long userId = 1L;
        Project project = new Project(null, "New Project", "Description", null, new AppUser(), Collections.emptySet());

        when(projectService.createProject(any(Project.class), any(Long.class))).thenReturn(project);

        mockMvc.perform(post("/projects/create/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Project"));
    }

    @Test
    void inviteMember_ReturnsUpdatedProject_WhenUserExists() throws Exception {
        Long projectId = 1L;
        Long userId = 2L;
        InviteRequestDTO inviteRequestDTO = new InviteRequestDTO("user@example.com");

        Project project = new Project();
        project.setId(projectId);

        when(projectService.addMemberToProject(projectId, inviteRequestDTO.getEmail())).thenReturn(project);

        mockMvc.perform(post("/projects/" + projectId + "/invite/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId));
    }

    @Test
    void inviteMember_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
        Long projectId = 1L;
        Long userId = 2L;
        InviteRequestDTO inviteRequestDTO = new InviteRequestDTO("nonexistent@example.com");

        when(projectService.addMemberToProject(projectId, inviteRequestDTO.getEmail())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/projects/" + projectId + "/invite/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found with email: " + inviteRequestDTO.getEmail()));
    }

    @Test
    void assignRoleToMember_ReturnsSuccessMessage_WhenValidInput() throws Exception {
        Long projectId = 1L;
        Long memberId = 2L;
        RoleAssignmentDTO roleAssignmentDTO = new RoleAssignmentDTO(Role.ADMIN);

        when(projectService.assignRoleToMember(eq(projectId), eq(memberId), eq(roleAssignmentDTO.getRole())))
                .thenReturn(new ProjectMemberRole());

        mockMvc.perform(put("/projects/" + projectId + "/assign-role/" + memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleAssignmentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated successfully"));
    }


    @Test
    void getAllProjects_ReturnsListOfProjects() throws Exception {
        Project project = new Project(null, "Project1", "Description", null, new AppUser(), Collections.emptySet());
        List<Project> projects = List.of(project);

        when(projectService.getAllProjects()).thenReturn(projects);

        mockMvc.perform(get("/projects/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Project1"));
    }

    @Test
    void getProjectsByUserId_ReturnsListOfProjects_WhenUserExists() throws Exception {
        Long userId = 1L;
        Project project = new Project(null, "User Project", "Description", null, new AppUser(), Collections.emptySet());
        List<Project> projects = List.of(project);

        when(projectService.getProjectsByUserId(userId)).thenReturn(projects);

        mockMvc.perform(get("/projects/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("User Project"));
    }

    @Test
    void getProjectMembers_ReturnsListOfMembers() throws Exception {
        Long projectId = 1L;
        MemberDTO member = new MemberDTO(2L, "MemberName", "member@example.com");
        List<MemberDTO> members = List.of(member);

        when(projectService.getProjectMembers(projectId)).thenReturn(members);

        mockMvc.perform(get("/projects/" + projectId + "/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("MemberName"));
    }

}

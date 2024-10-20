package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.dto.MemberDTO;
import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.entity.Project;
import com.visiplus.pmt.entity.ProjectMemberRole;
import com.visiplus.pmt.enums.Role;
import com.visiplus.pmt.exception.UserNotFoundException;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.repository.ProjectMemberRoleRepository;
import com.visiplus.pmt.repository.ProjectRepository;
import com.visiplus.pmt.service.ProjectService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    // Repositories for interacting with database entities
    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;

    // Constructor-based dependency injection for repositories
    public ProjectServiceImpl(ProjectRepository projectRepository, AppUserRepository appUserRepository, ProjectMemberRoleRepository projectMemberRoleRepository) {
        this.projectRepository = projectRepository;
        this.appUserRepository = appUserRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
    }

    /**
     * Creates a new project and assigns the given user as the project owner.
     *
     * @param project the project to be created
     * @param userId  the ID of the user who owns the project
     * @return the saved project with the owner and default role assigned
     * @throws RuntimeException if the user with the given ID is not found
     */
    @Transactional
    public Project createProject(Project project, Long userId) {
        // Find the user by ID or throw an exception if not found
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("AppUser not found with id: " + userId));

        // Set the user as the project owner
        project.setOwner(appUser);

        // Save the project and persist it in the database
        Project savedProject = projectRepository.save(project);

        // Create a ProjectMemberRole and assign the owner as ADMIN
        ProjectMemberRole memberRole = new ProjectMemberRole();
        memberRole.setProject(savedProject);
        memberRole.setMember(appUser);
        memberRole.setRole(Role.ADMIN);

        // Save the member role for the project owner
        projectMemberRoleRepository.save(memberRole);

        return savedProject;
    }


    /**
     * Adds a new member to the project by their email.
     *
     * @param projectId the ID of the project to which the member is being added
     * @param email     the email of the user to be added
     * @return the project after adding the new member
     * @throws ResponseStatusException if the project or user is not found, or if the user is already a member
     */
    @Transactional
    @Override
    public Project addMemberToProject(Long projectId, String email) {
        // Find the project by ID or throw an exception if not found
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        // Find the user by email or throw a custom exception if not found
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Check if the user is already a member of the project
        Optional<ProjectMemberRole> existingMember = projectMemberRoleRepository
                .findByProjectIdAndMemberId(projectId, user.getId());

        if (existingMember.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email " + email + " is already a member of this project.");
        }

        // Create and assign the member role to the user as MEMBER
        ProjectMemberRole memberRole = new ProjectMemberRole();
        memberRole.setProject(project);
        memberRole.setMember(user);
        memberRole.setRole(Role.MEMBER);

        // Add the member role to the project's list of members with roles
        project.getMembersWithRoles().add(memberRole);

        // Save the new member role to the database
        projectMemberRoleRepository.save(memberRole);

        return project;
    }

    /**
     * Assigns a new role to an existing project member.
     *
     * @param projectId the ID of the project
     * @param memberId  the ID of the member whose role is being updated
     * @param role      the new role to be assigned
     * @return the updated ProjectMemberRole after the role change
     * @throws RuntimeException if the member is not found in the project
     */
    @Override
    public ProjectMemberRole assignRoleToMember(Long projectId, Long memberId, Role role) {
        return projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, memberId)
                .map(memberRole -> {
                    // Update the role of the member
                    memberRole.setRole(role);
                    return projectMemberRoleRepository.save(memberRole);
                })
                .orElseThrow(() -> new RuntimeException("Member with ID " + memberId + " not found in project with ID " + projectId));
    }

    /**
     * Retrieves a project by its unique ID.
     *
     * @param projectId the ID of the project to retrieve
     * @return the project if found
     * @throws RuntimeException if the project does not exist
     */
    @Override
    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project doesn't exist"));
    }

    @Override
    public List<Project> getAllProjects() {
        return (List<Project>) projectRepository.findAll();
    }

    @Override
    public List<Project> getProjectsByUserId(Long userId) {
        return projectRepository.findProjectsByUserId(userId);
    }

    @Override
    public List<MemberDTO> getProjectMembers(Long projectId) {
        List<ProjectMemberRole> projectMembers = projectMemberRoleRepository.findByProjectId(projectId);
        return projectMembers.stream()
                .map(memberRole -> new MemberDTO(memberRole.getMember().getId(), memberRole.getMember().getUsername(), memberRole.getMember().getEmail()))
                .collect(Collectors.toList());
    }
}

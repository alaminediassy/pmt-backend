package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.entity.Project;
import com.visiplus.pmt.entity.ProjectMemberRole;
import com.visiplus.pmt.enums.Role;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.repository.ProjectMemberRoleRepository;
import com.visiplus.pmt.repository.ProjectRepository;
import com.visiplus.pmt.service.ProjectService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, AppUserRepository appUserRepository, ProjectMemberRoleRepository projectMemberRoleRepository) {
        this.projectRepository = projectRepository;
        this.appUserRepository = appUserRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
    }
    @Override
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }


    @Transactional
    @Override
    public Project addMemberToProject(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Optional<ProjectMemberRole> existingMember = projectMemberRoleRepository
                .findByProjectIdAndMemberId(projectId, user.getId());

        if (existingMember.isPresent()) {
            throw new RuntimeException("User with email " + email + " is already a member of this project.");
        }

        ProjectMemberRole memberRole = new ProjectMemberRole();
        memberRole.setProject(project);
        memberRole.setMember(user);
        memberRole.setRole(Role.MEMBER);

        project.getMembersWithRoles().add(memberRole);

        projectMemberRoleRepository.save(memberRole);

        project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found after saving member"));

        System.out.println("Ajout de l'utilisateur avec l'email " + email + " au projet " + project.getName());

        return project;
    }


    @Override
    public ProjectMemberRole assignRoleToMember(Long projectId, Long memberId, Role role) {
        return projectMemberRoleRepository.findByProjectIdAndMemberId(projectId, memberId)
                .map(memberRole -> {
                    memberRole.setRole(role);
                    return projectMemberRoleRepository.save(memberRole);
                })
                .orElseThrow(() -> new RuntimeException("Member with ID " + memberId + " not found in project with ID " + projectId));
    }

    @Override
    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project doesn't exist"));
    }
}

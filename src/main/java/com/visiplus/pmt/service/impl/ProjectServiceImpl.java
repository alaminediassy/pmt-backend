package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.entity.Project;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.repository.ProjectRepository;
import com.visiplus.pmt.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, AppUserRepository appUserRepository) {
        this.projectRepository = projectRepository;
        this.appUserRepository = appUserRepository;
    }
    @Override
    public Project createdProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public Project addMemberToProject(Long projectId, String email) {
        // Verify if project exist
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Project not found");
        }
        Project project = projectOpt.get();

        // Check if the user with this email already exists in pmt
        Optional<AppUser> userOpt = appUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User with email " + email + " does not exist in the company.");
        }
        AppUser user = userOpt.get();

        // Add the user to the project
        project.getMembers().add(user);
        return projectRepository.save(project);
    }

    @Override
    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project doesn't exist"));
    }
}

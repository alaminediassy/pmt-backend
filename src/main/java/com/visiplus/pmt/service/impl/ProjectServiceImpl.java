package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.entity.Project;
import com.visiplus.pmt.repository.ProjectRepository;
import com.visiplus.pmt.service.ProjectService;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
    @Override
    public Project createdProject(Project project) {
        return projectRepository.save(project);
    }
}

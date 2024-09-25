package com.visiplus.pmt.service;

import com.visiplus.pmt.entity.Project;

public interface ProjectService {
    Project createdProject(Project project);
    Project addMemberToProject(Long projectId, String email);

    Project getProjectById(Long projectId);
}

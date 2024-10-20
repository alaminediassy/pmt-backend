package com.visiplus.pmt.service;

import com.visiplus.pmt.dto.MemberDTO;
import com.visiplus.pmt.entity.Project;
import com.visiplus.pmt.entity.ProjectMemberRole;
import com.visiplus.pmt.enums.Role;

import java.util.List;

public interface ProjectService {
    Project createProject(Project project, Long userId);
    Project addMemberToProject(Long projectId, String email);
    ProjectMemberRole assignRoleToMember(Long projectId, Long memberId, Role role);
    Project getProjectById(Long projectId);
    List<Project> getAllProjects();
    List<Project> getProjectsByUserId(Long userId);
    List<MemberDTO> getProjectMembers(Long projectId);
}

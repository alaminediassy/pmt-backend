package com.visiplus.pmt.controller;

import com.visiplus.pmt.dto.InviteRequestDTO;
import com.visiplus.pmt.dto.RoleAssignmentDTO;
import com.visiplus.pmt.entity.Project;
import com.visiplus.pmt.exception.UserNotFoundException;
import com.visiplus.pmt.service.AppUserService;
import com.visiplus.pmt.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final AppUserService appUserService;

    public ProjectController(ProjectService projectService, AppUserService appUserService) {
        this.projectService = projectService;
        this.appUserService = appUserService;
    }

    // Endpoint to create a project
    @PostMapping("/create/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createProject(@RequestBody Project project, @PathVariable Long userId) {
        try {
            Project createdProject = projectService.createProject(project, userId);

            return ResponseEntity.ok(createdProject);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PostMapping("/{projectId}/invite/{userId}")
    public ResponseEntity<?> inviteMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody InviteRequestDTO inviteRequestDTO) {
        try {
            Project updatedProject = projectService.addMemberToProject(projectId, inviteRequestDTO.getEmail());
            return ResponseEntity.ok(updatedProject);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with email: " + inviteRequestDTO.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing request: " + e.getMessage());
        }
    }


    // Endpoint to assign role to project member
    @PutMapping("/{projectId}/assign-role/{memberId}")
    public ResponseEntity<String> assignRoleToMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestBody RoleAssignmentDTO roleAssignmentDTO) {
        try {
            projectService.assignRoleToMember(projectId, memberId, roleAssignmentDTO.getRole());
            return ResponseEntity.ok("Role updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

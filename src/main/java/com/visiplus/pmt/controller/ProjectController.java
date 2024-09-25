package com.visiplus.pmt.controller;

import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.entity.Project;
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

    @PostMapping("/create/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createdProject(@RequestBody Project project, @PathVariable Long userId) {
        try {
            AppUser owner = appUserService.findUserById(userId);
            if (owner == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            project.setOwner(owner);

            Project createdProject = projectService.createdProject(project);
            return ResponseEntity.ok(String.format("Project '%s' created successfully", createdProject.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

package com.visiplus.pmt.controller;

import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.service.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    // Endpoint to create user
    @PostMapping(path = "/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> registerAppUser(@RequestBody AppUser appUser) {
        try {
            AppUser createAppUser = appUserService.registerAppUser(appUser);
            return ResponseEntity.ok(createAppUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Endpoint to connect
    @PostMapping(path = "/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> loginAppUser(@RequestBody AppUser appUser) {
        try {
            AppUser loggedInUser = appUserService.loginAppUser(appUser.getEmail(), appUser.getPassword());
            return ResponseEntity.ok(Collections.singletonMap("message", "User logged in successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

}

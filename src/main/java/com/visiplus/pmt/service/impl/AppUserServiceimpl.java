package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.service.AppUserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppUserServiceimpl implements AppUserService {
    // Dependency injection for the AppUserRepository
    private final AppUserRepository appUserRepository;

    // Constructor to inject the AppUserRepository
    public AppUserServiceimpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /**
     * Registers a new user in the system.
     * Validates the email format and checks if the email is already used.
     *
     * @param appUser the user to be registered
     * @return the registered user
     * @throws RuntimeException if the email format is invalid or the email is already used
     */
    @Override
    public AppUser registerAppUser(AppUser appUser) {
        // Validate email format
        if (!appUser.getEmail().contains("@") || !appUser.getEmail().contains(".")){
            throw new RuntimeException("Invalid email format");
        }

        // Check if email already exists in the system
        if (appUserRepository.findByEmail(appUser.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Save and return the registered user
        return appUserRepository.save(appUser);
    }

    /**
     * Logs in an existing user.
     * Validates the email and password provided by the user.
     *
     * @param email the user's email
     * @param password the user's password
     * @return the authenticated user
     * @throws RuntimeException if the email or password is invalid
     */
    @Override
    public AppUser loginAppUser(String email, String password) {
        Optional<AppUser> appUser = appUserRepository.findByEmail(email);

        // Check if the email exists and the password matches
        if (appUser.isPresent() && appUser.get().getPassword().equals(password)) {
            return appUser.get();
        } else {
            throw new RuntimeException("Invalid email or password");
        }
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId the user's ID
     * @return the user if found, or null if the user does not exist
     */
    @Override
    public AppUser findUserById(Long userId) {
        return appUserRepository.findById(userId).orElse(null);
    }
}

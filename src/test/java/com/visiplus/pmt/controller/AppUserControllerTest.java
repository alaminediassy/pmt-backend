package com.visiplus.pmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.service.AppUserService;
import com.visiplus.pmt.jwt.JwtAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppUserController.class)
public class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserService appUserService;

    @MockBean
    private JwtAuthService jwtAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUser_ReturnsCreated_WhenValidInput() throws Exception {
        // Arrange
        AppUser appUser = new AppUser(null, "username", "new@example.com", "password", null);
        when(appUserService.registerAppUser(any(AppUser.class))).thenReturn(appUser);

        // Act & Assert
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void registerUser_ReturnsBadRequest_WhenEmailExists() throws Exception {
        // Arrange
        AppUser appUser = new AppUser(null, "username", "existing@example.com", "password", null);
        when(appUserService.registerAppUser(any(AppUser.class))).thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void loginUser_ReturnsToken_WhenCredentialsAreValid() throws Exception {
        // Arrange
        AppUser appUser = new AppUser(null, "username", "user@example.com", "password", "fake-jwt-token");
        when(appUserService.loginAppUser(appUser.getEmail(), appUser.getPassword())).thenReturn(appUser);

        // Act & Assert
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void loginUser_ReturnsBadRequest_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        AppUser appUser = new AppUser(null, "username", "user@example.com", "wrongpassword", null);
        when(appUserService.loginAppUser(appUser.getEmail(), appUser.getPassword()))
                .thenThrow(new RuntimeException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void logoutUser_ReturnsSuccessMessage_WhenTokenIsValid() throws Exception {
        // Arrange
        String token = "Bearer fake-jwt-token";
        doNothing().when(appUserService).logoutUser("fake-jwt-token");
        when(jwtAuthService.verifyToken("fake-jwt-token")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/users/logout")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User logged out successfully"));
    }

    @Test
    void logoutUser_ReturnsUnauthorized_WhenTokenIsInvalid() throws Exception {
        // Arrange
        String token = "Bearer invalid-token";

        when(jwtAuthService.verifyToken("invalid-token")).thenAnswer(invocation ->
                ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid token"))
        );

        // Act & Assert
        mockMvc.perform(post("/users/logout")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }


}

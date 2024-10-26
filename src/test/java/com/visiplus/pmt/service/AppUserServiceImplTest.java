package com.visiplus.pmt.service;

import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.jwt.JwtBlacklistService;
import com.visiplus.pmt.jwt.JwtService;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.service.impl.AppUserServiceimpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserServiceimpl appUserService;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtBlacklistService jwtBlacklistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_ThrowsIfEmailExists() {
        // Arrange
        AppUser appUser = new AppUser(null, "username", "existing@gmail.com", "password", null);
        when(appUserRepository.findByEmail("existing@gmail.com")).thenReturn(Optional.of(appUser));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            appUserService.registerAppUser(appUser);
        });
        assertEquals("Email already exists", exception.getMessage());
        verify(appUserRepository, never()).save(appUser);
    }

    @Test
    void registerUser_SavesIfEmailNotExists() {
        // Arrange
        AppUser appUser = new AppUser(null, "username", "new@example.com", "password", null);
        when(appUserRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(appUserRepository.save(appUser)).thenReturn(appUser);

        // Act
        AppUser savedUser = appUserService.registerAppUser(appUser);

        // Assert
        assertEquals("new@example.com", savedUser.getEmail());
        verify(appUserRepository, times(1)).save(appUser);
    }

    @Test
    void loginAppUser_ShouldReturnUserWithToken_WhenCredentialsAreValid() {
        // Arrange
        AppUser appUser = new AppUser(1L, "username", "user@example.com", "password", null);
        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(appUser));
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("fake-jwt-token");

        // Act
        AppUser result = appUserService.loginAppUser("user@example.com", "password");

        // Assert
        assertEquals("fake-jwt-token", result.getToken());
        assertEquals("user@example.com", result.getEmail());
        verify(jwtService, times(1)).generateToken(any(), any(), any(), any());
    }

    @Test
    void loginAppUser_ShouldThrowException_WhenCredentialsAreInvalid() {
        // Arrange
        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            appUserService.loginAppUser("user@example.com", "password");
        });
        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtService, never()).generateToken(any(), any(), any(), any());
    }

    @Test
    void logoutUser_ShouldBlacklistToken() {
        // Arrange
        String token = "fake-jwt-token";

        // Act
        appUserService.logoutUser(token);

        // Assert
        verify(jwtBlacklistService, times(1)).blacklistToken(token);
    }
}

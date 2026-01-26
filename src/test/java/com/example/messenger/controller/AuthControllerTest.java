package com.example.messenger.controller;

import com.example.messenger.api.AuthController;
import com.example.messenger.api.dto.AuthDtos;
import com.example.messenger.security.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("register() should delegate to AuthService.register and return token response")
    void register_shouldDelegateToService() {
        // Given
        AuthDtos.RegisterRequest req = mock(AuthDtos.RegisterRequest.class);
        AuthDtos.TokenResponse expected = mock(AuthDtos.TokenResponse.class);

        when(authService.register(req)).thenReturn(expected);

        // When
        AuthDtos.TokenResponse res = authController.register(req);

        // Then
        assertThat(res).isSameAs(expected);
        verify(authService, times(1)).register(req);
        verifyNoMoreInteractions(authService);
    }

    @Test
    @DisplayName("login() should delegate to AuthService.login and return token response")
    void login_shouldDelegateToService() {
        // Given
        AuthDtos.LoginRequest req = mock(AuthDtos.LoginRequest.class);
        AuthDtos.TokenResponse expected = mock(AuthDtos.TokenResponse.class);

        when(authService.login(req)).thenReturn(expected);

        // When
        AuthDtos.TokenResponse res = authController.login(req);

        // Then
        assertThat(res).isSameAs(expected);
        verify(authService, times(1)).login(req);
        verifyNoMoreInteractions(authService);
    }

    @Test
    @DisplayName("register() should propagate exception thrown by service")
    void register_shouldPropagateException() {
        // Given
        AuthDtos.RegisterRequest req = mock(AuthDtos.RegisterRequest.class);
        RuntimeException ex = new RuntimeException("boom");

        when(authService.register(req)).thenThrow(ex);

        // When / Then
        RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> authController.register(req)
        );
        assertThat(thrown).isSameAs(ex);

        verify(authService, times(1)).register(req);
        verifyNoMoreInteractions(authService);
    }

    @Test
    @DisplayName("login() should propagate exception thrown by service")
    void login_shouldPropagateException() {
        // Given
        AuthDtos.LoginRequest req = mock(AuthDtos.LoginRequest.class);
        RuntimeException ex = new RuntimeException("boom");

        when(authService.login(req)).thenThrow(ex);

        // When / Then
        RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> authController.login(req)
        );
        assertThat(thrown).isSameAs(ex);

        verify(authService, times(1)).login(req);
        verifyNoMoreInteractions(authService);
    }
}
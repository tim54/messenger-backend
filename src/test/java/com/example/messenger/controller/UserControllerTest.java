package com.example.messenger.controller;

import com.example.messenger.api.UserController;
import com.example.messenger.api.dto.UserDtos;
import com.example.messenger.repo.api.GenericUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private GenericUserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private User principal;

    @BeforeEach
    void setUp() {
        principal = new User("testuser", "pw", Collections.emptyList());
    }

    @Test
    @DisplayName("me() should return current user mapped to DTO")
    void me_shouldReturnMappedUser() {
        // Given
        UUID id = UUID.randomUUID();
        var domainUser = new com.example.messenger.domain.User();
        domainUser.setId(id);
        domainUser.setUsername("testuser");
        domainUser.setDisplayName("Test User");
        domainUser.setAvatarUrl("https://cdn.example/avatar.png");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));

        // When
        UserDtos.userResponse res = userController.me(principal);

        // Then
        assertThat(res).isNotNull();
        assertThat(res.id()).isEqualTo(id);
        assertThat(res.username()).isEqualTo("testuser");
        assertThat(res.displayName()).isEqualTo("Test User");
        assertThat(res.avatarUrl()).isEqualTo("https://cdn.example/avatar.png");

        verify(userRepository, times(1)).findByUsername("testuser");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("me() should throw when current user not found")
    void me_whenUserNotFound_shouldThrow() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userController.me(principal))
                .isInstanceOf(java.util.NoSuchElementException.class);

        verify(userRepository, times(1)).findByUsername("testuser");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("getAllUsers() should map all users to DTO list")
    void getAllUsers_shouldMapAllUsers() {
        // Given
        var u1 = new com.example.messenger.domain.User();
        u1.setId(UUID.randomUUID());
        u1.setUsername("u1");
        u1.setDisplayName("User 1");
        u1.setAvatarUrl(null);

        var u2 = new com.example.messenger.domain.User();
        u2.setId(UUID.randomUUID());
        u2.setUsername("u2");
        u2.setDisplayName("User 2");
        u2.setAvatarUrl("a2");

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        // When
        List<UserDtos.userResponse> res = userController.getAllUsers();

        // Then
        assertThat(res).hasSize(2);

        assertThat(res.get(0).id()).isEqualTo(u1.getId());
        assertThat(res.get(0).username()).isEqualTo("u1");
        assertThat(res.get(0).displayName()).isEqualTo("User 1");
        assertThat(res.get(0).avatarUrl()).isNull();

        assertThat(res.get(1).id()).isEqualTo(u2.getId());
        assertThat(res.get(1).username()).isEqualTo("u2");
        assertThat(res.get(1).displayName()).isEqualTo("User 2");
        assertThat(res.get(1).avatarUrl()).isEqualTo("a2");

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("getAllUsers() should return empty list when repository returns none")
    void getAllUsers_whenEmpty_shouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<UserDtos.userResponse> res = userController.getAllUsers();

        // Then
        assertThat(res).isNotNull();
        assertThat(res).isEmpty();

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }
}

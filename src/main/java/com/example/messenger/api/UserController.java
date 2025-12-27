
package com.example.messenger.api;

import com.example.messenger.api.dto.UserDtos;
import com.example.messenger.repo.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) { this.userRepository = userRepository; }

    @GetMapping("/me")
    public UserDtos.userResponse me(@AuthenticationPrincipal User principal) {
        var u = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return new UserDtos.userResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarUrl());
    }

    @GetMapping("/allUsers")
    public List<UserDtos.userResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDtos.userResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarUrl()))
                .toList();
    }
}

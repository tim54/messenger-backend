
package com.example.messenger.security;

import com.example.messenger.api.dto.AuthDtos;
import com.example.messenger.domain.User;
import com.example.messenger.repo.api.GenericUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final GenericUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(GenericUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new IllegalArgumentException("username taken");
        }
        User u = new User();
        u.setUsername(req.username());
        u.setDisplayName(req.displayName());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        userRepository.save(u);
        return new AuthDtos.TokenResponse(
                jwtService.generateAccessToken(u.getId(), u.getUsername()),
                jwtService.generateRefreshToken(u.getId(), u.getUsername())
        );
    }

    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req) {
        var user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid credentials");
        }
        return new AuthDtos.TokenResponse(
                jwtService.generateAccessToken(user.getId(), user.getUsername()),
                jwtService.generateRefreshToken(user.getId(), user.getUsername())
        );
    }
}

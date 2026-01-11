package com.example.messenger.repo.api;

import com.example.messenger.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenericUserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void deleteById(UUID id);
    boolean existsByUsername(String username);
}
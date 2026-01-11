package com.example.messenger.repo.dynamodb.mapper;

import com.example.messenger.domain.User;
import com.example.messenger.repo.dynamodb.model.UserDynamoModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {
    public UserDynamoModel toModel(User user) {
        UserDynamoModel model = new UserDynamoModel();
        model.setId(user.getId() != null ? user.getId().toString() : UUID.randomUUID().toString());
        model.setUsername(user.getUsername());
        model.setDisplayName(user.getDisplayName());
        model.setPasswordHash(user.getPasswordHash());
        model.setCreatedAt(user.getCreatedAt());
//        model.setLastSeenAt(user.getLastSeenAt());
        return model;
    }

    public User toDomain(UserDynamoModel model) {
        User user = new User();
        user.setId(UUID.fromString(model.getId()));
        user.setUsername(model.getUsername());
        user.setDisplayName(model.getDisplayName());
        user.setPasswordHash(model.getPasswordHash());
        user.setCreatedAt(model.getCreatedAt());
//        user.setLastSeenAt(model.getLastSeenAt());
        return user;
    }
}
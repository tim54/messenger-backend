
package com.example.messenger.api.dto;

import java.util.UUID;

public class UserDtos {
    public record userResponse(UUID id, String username, String displayName, String avatarUrl) {}
}

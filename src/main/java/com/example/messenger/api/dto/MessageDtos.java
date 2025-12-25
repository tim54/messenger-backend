
package com.example.messenger.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public class MessageDtos {
    public record SendMessageRequest(@NotBlank String content) {}
    public record MessageResponse(UUID id, UUID conversationId, UUID senderId, String content, Instant createdAt) {}
}

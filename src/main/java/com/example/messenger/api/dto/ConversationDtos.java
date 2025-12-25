
package com.example.messenger.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class ConversationDtos {
    public record CreateConversationRequest(boolean direct, List<UUID> memberIds) {}
    public record ConversationResponse(UUID id, boolean direct) {}
}

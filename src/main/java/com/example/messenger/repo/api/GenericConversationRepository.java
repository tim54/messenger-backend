package com.example.messenger.repo.api;

import com.example.messenger.domain.Conversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenericConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(UUID id);
    List<Conversation> findAll();
    void deleteById(UUID id);
}
package com.example.messenger.repo.api;

import com.example.messenger.domain.Message;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenericMessageRepository {
    Message save(Message message);
    Optional<Message> findById(UUID id);
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
    List<Message> findByConversationIdAndCreatedAtBefore(UUID conversationId, Instant before, int limit);
    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);
    void deleteById(UUID id);
}
package com.example.messenger.repo.api;

import com.example.messenger.domain.ConversationMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenericConversationMemberRepository {
    ConversationMember save(ConversationMember member);

    Optional<ConversationMember> findById(UUID id);

    List<ConversationMember> findByConversationId(UUID conversationId);

    List<ConversationMember> findByUserId(UUID userId);

    Optional<ConversationMember> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    void deleteById(UUID id);
}
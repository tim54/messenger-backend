
package com.example.messenger.repo;

import com.example.messenger.domain.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {
    List<ConversationMember> findByUserId(UUID userId);
    List<ConversationMember> findByConversationId(UUID conversationId);
    @Modifying
    void deleteByConversationId(UUID conversationId);
}

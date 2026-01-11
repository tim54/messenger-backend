package com.example.messenger.repo.postgres;

import com.example.messenger.domain.ConversationMember;
import com.example.messenger.repo.ConversationMemberRepository;
import com.example.messenger.repo.api.GenericConversationMemberRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class PostgresConversationMemberRepositoryAdapter implements GenericConversationMemberRepository {

    private final ConversationMemberRepository jpaRepository;

    public PostgresConversationMemberRepositoryAdapter(ConversationMemberRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ConversationMember save(ConversationMember member) {
        return jpaRepository.save(member);
    }

    @Override
    public Optional<ConversationMember> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ConversationMember> findByConversationId(UUID conversationId) {
        return jpaRepository.findByConversationId(conversationId);
    }

    @Override
    public List<ConversationMember> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<ConversationMember> findByConversationIdAndUserId(UUID conversationId, UUID userId) {
        // Spring Data method is not present on the JPA repo; implement via filtering.
        // If you want it more efficient, add a derived query method to ConversationMemberRepository.
        return jpaRepository.findByConversationId(conversationId).stream()
                .filter(m -> userId.equals(m.getUserId()))
                .findFirst();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
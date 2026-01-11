package com.example.messenger.repo.postgres;

import com.example.messenger.domain.Conversation;
import com.example.messenger.repo.ConversationRepository;
import com.example.messenger.repo.api.GenericConversationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class PostgresConversationRepositoryAdapter implements GenericConversationRepository {

    private final ConversationRepository jpaRepository;

    public PostgresConversationRepositoryAdapter(ConversationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Conversation save(Conversation conversation) {
        return jpaRepository.save(conversation);
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Conversation> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
package com.example.messenger.repo.postgres;

import com.example.messenger.domain.Message;
import com.example.messenger.repo.MessageRepository;
import com.example.messenger.repo.api.GenericMessageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class PostgresMessageRepositoryAdapter implements GenericMessageRepository {

    private final MessageRepository jpaRepository;

    @PersistenceContext private EntityManager entityManager;

    public PostgresMessageRepositoryAdapter(MessageRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Message save(Message message) {
        return jpaRepository.save(message);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId) {
        TypedQuery<Message> q =
                entityManager.createQuery(
                        """
                        select m
                        from Message m
                        where m.conversationId = :conversationId
                        order by m.createdAt asc
                        """,
                        Message.class);

        return q.setParameter("conversationId", conversationId).getResultList();
    }

    @Override
    public List<Message> findByConversationIdAndCreatedAtBefore(
            UUID conversationId, Instant before, int limit) {
        TypedQuery<Message> q =
                entityManager.createQuery(
                        """
                        select m
                        from Message m
                        where m.conversationId = :conversationId
                          and m.createdAt < :before
                        order by m.createdAt desc
                        """,
                        Message.class);

        return q.setParameter("conversationId", conversationId)
                .setParameter("before", before)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable) {
        return jpaRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
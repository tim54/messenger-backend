package com.example.messenger.repo.dynamodb;

import com.example.messenger.domain.Conversation;
import com.example.messenger.repo.api.GenericConversationRepository;
import com.example.messenger.repo.dynamodb.mapper.ConversationMapper;
import com.example.messenger.repo.dynamodb.model.ConversationDynamoModel;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@Profile("dynamodb")
public class DynamoDbConversationRepository implements GenericConversationRepository {

    private static final String TABLE_NAME = "Conversations";

    private final DynamoDbTable<ConversationDynamoModel> conversationTable;
    private final ConversationMapper mapper;

    public DynamoDbConversationRepository(DynamoDbEnhancedClient enhancedClient, ConversationMapper mapper) {
        this.conversationTable =
                enhancedClient.table(TABLE_NAME, TableSchema.fromBean(ConversationDynamoModel.class));
        this.mapper = mapper;
    }

    @Override
    public Conversation save(Conversation conversation) {
        ConversationDynamoModel model = mapper.toModel(conversation);

        // Ensure createdAt exists (domain has @PrePersist, but Dynamo path bypasses JPA)
        if (model.getCreatedAt() == null) {
            model.setCreatedAt(Instant.now());
        }

        conversationTable.putItem(model);
        return mapper.toDomain(model);
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        ConversationDynamoModel model =
                conversationTable.getItem(r -> r.key(k -> k.partitionValue(id.toString())));
        return Optional.ofNullable(model).map(mapper::toDomain);
    }

    @Override
    public List<Conversation> findAll() {
        return conversationTable.scan().items().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        conversationTable.deleteItem(r -> r.key(k -> k.partitionValue(id.toString())));
    }
}
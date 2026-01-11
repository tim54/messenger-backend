package com.example.messenger.repo.dynamodb;

import com.example.messenger.domain.Message;
import com.example.messenger.repo.api.GenericMessageRepository;
import com.example.messenger.repo.dynamodb.mapper.MessageMapper;
import com.example.messenger.repo.dynamodb.model.MessageDynamoModel;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
@Profile("dynamodb")
public class DynamoDbMessageRepository implements GenericMessageRepository {

    private static final String TABLE_NAME = "Messages";
    private static final String CONVERSATION_CREATED_INDEX = "conversation-created-index";

    private final DynamoDbTable<MessageDynamoModel> messageTable;
    private final MessageMapper mapper;

    public DynamoDbMessageRepository(DynamoDbEnhancedClient enhancedClient, MessageMapper mapper) {
        this.messageTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(MessageDynamoModel.class));
        this.mapper = mapper;
    }

    @Override
    public Message save(Message message) {
        MessageDynamoModel model = mapper.toModel(message);

        // Ensure createdAt exists for proper sorting on the GSI
        if (model.getCreatedAt() == null) {
            model.setCreatedAt(Instant.now());
        }

        messageTable.putItem(model);
        return mapper.toDomain(model);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        MessageDynamoModel model =
                messageTable.getItem(r -> r.key(k -> k.partitionValue(id.toString())));
        return Optional.ofNullable(model).map(mapper::toDomain);
    }

    @Override
    public List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId) {
        DynamoDbIndex<MessageDynamoModel> index = messageTable.index(CONVERSATION_CREATED_INDEX);

        return index
                .query(
                        r ->
                                r.queryConditional(
                                                QueryConditional.keyEqualTo(
                                                        Key.builder().partitionValue(conversationId.toString()).build()))
                                        .scanIndexForward(true))
                .stream()
                .flatMap(page -> page.items().stream())
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationIdAndCreatedAtBefore(
            UUID conversationId, Instant before, int limit) {
        DynamoDbIndex<MessageDynamoModel> index = messageTable.index(CONVERSATION_CREATED_INDEX);

        return index
                .query(
                        r ->
                                r.queryConditional(
                                                QueryConditional.sortLessThan(
                                                        Key.builder()
                                                                .partitionValue(conversationId.toString())
                                                                .sortValue(String.valueOf(before))
                                                                .build()))
                                        // Usually you want "latest before X"
                                        .scanIndexForward(false)
                                        .limit(limit))
                .stream()
                .flatMap(page -> page.items().stream())
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable) {
        DynamoDbIndex<MessageDynamoModel> index = messageTable.index(CONVERSATION_CREATED_INDEX);

        int limit = pageable != null ? pageable.getPageSize() : 50;

        // Note: Pageable offset is not supported here without passing an ExclusiveStartKey (cursor).
        return index
                .query(
                        r ->
                                r.queryConditional(
                                                QueryConditional.keyEqualTo(
                                                        Key.builder().partitionValue(conversationId.toString()).build()))
                                        .scanIndexForward(false)
                                        .limit(limit))
                .stream()
                .flatMap(page -> page.items().stream())
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        messageTable.deleteItem(r -> r.key(k -> k.partitionValue(id.toString())));
    }
}
package com.example.messenger.repo.dynamodb;

import com.example.messenger.domain.ConversationMember;
import com.example.messenger.repo.api.GenericConversationMemberRepository;
import com.example.messenger.repo.dynamodb.mapper.ConversationMemberMapper;
import com.example.messenger.repo.dynamodb.model.ConversationMemberDynamoModel;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
@Profile("dynamodb")
public class DynamoDbConversationMemberRepository implements GenericConversationMemberRepository {

    private static final String TABLE_NAME = "ConversationMembers";

    private static final String CONVERSATION_INDEX = "conversation-index";
    private static final String USER_INDEX = "user-index";
    private static final String CONVERSATION_USER_INDEX = "conversation-user-index";

    private final DynamoDbTable<ConversationMemberDynamoModel> memberTable;
    private final ConversationMemberMapper mapper;

    public DynamoDbConversationMemberRepository(
            DynamoDbEnhancedClient enhancedClient, ConversationMemberMapper mapper) {
        this.memberTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(ConversationMemberDynamoModel.class));
        this.mapper = mapper;
    }

    @Override
    public ConversationMember save(ConversationMember member) {
        ConversationMemberDynamoModel model = mapper.toModel(member);

        // Ensure joinedAt exists (domain uses @PrePersist, but Dynamo path bypasses JPA)
        if (model.getJoinedAt() == null) {
            model.setJoinedAt(Instant.now());
        }

        memberTable.putItem(model);
        return mapper.toDomain(model);
    }

    @Override
    public Optional<ConversationMember> findById(UUID id) {
        ConversationMemberDynamoModel model =
                memberTable.getItem(r -> r.key(k -> k.partitionValue(id.toString())));
        return Optional.ofNullable(model).map(mapper::toDomain);
    }

    @Override
    public List<ConversationMember> findByConversationId(UUID conversationId) {
        DynamoDbIndex<ConversationMemberDynamoModel> index = memberTable.index(CONVERSATION_INDEX);

        return index
                .query(
                        r ->
                                r.queryConditional(
                                        QueryConditional.keyEqualTo(
                                                Key.builder().partitionValue(conversationId.toString()).build())))
                .stream()
                .flatMap(page -> page.items().stream())
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationMember> findByUserId(UUID userId) {
        DynamoDbIndex<ConversationMemberDynamoModel> index = memberTable.index(USER_INDEX);

        return index
                .query(
                        r ->
                                r.queryConditional(
                                        QueryConditional.keyEqualTo(Key.builder().partitionValue(userId.toString()).build())))
                .stream()
                .flatMap(page -> page.items().stream())
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ConversationMember> findByConversationIdAndUserId(UUID conversationId, UUID userId) {
        DynamoDbIndex<ConversationMemberDynamoModel> index = memberTable.index(CONVERSATION_USER_INDEX);

        List<ConversationMemberDynamoModel> results =
                index
                        .query(
                                r ->
                                        r.queryConditional(
                                                        QueryConditional.keyEqualTo(
                                                                Key.builder()
                                                                        .partitionValue(conversationId.toString())
                                                                        .sortValue(userId.toString())
                                                                        .build())))
                                                .stream()
                                                .flatMap(page -> page.items().stream())
                                                .collect(Collectors.toList());

        return results.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(results.getFirst()));
    }

    @Override
    public void deleteById(UUID id) {
        memberTable.deleteItem(r -> r.key(k -> k.partitionValue(id.toString())));
    }
}
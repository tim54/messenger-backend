package com.example.messenger.repo;

import com.example.messenger.domain.ConversationMember;
import com.example.messenger.repo.api.GenericConversationMemberRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles({"test", "dynamodb"})
class DynamoDbConversationMemberRepositoryTest {

    private static final String TABLE_NAME = "ConversationMembers";
    private static final String CONVERSATION_INDEX = "conversation-index";
    private static final String USER_INDEX = "user-index";
    private static final String CONVERSATION_USER_INDEX = "conversation-user-index";

    @Container
    static final LocalStackContainer localstack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                    .withServices(LocalStackContainer.Service.DYNAMODB);

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("aws.region", localstack::getRegion);
        r.add("aws.accessKeyId", () -> localstack.getAccessKey());
        r.add("aws.secretAccessKey", () -> localstack.getSecretKey());
        r.add("aws.dynamodb.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    }

    @Autowired
    GenericConversationMemberRepository repo;

    @Autowired
    DynamoDbClient dynamoDbClient;

    @Autowired
    DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @BeforeEach
    void ensureTable() {
        if (!tableExists(TABLE_NAME)) {
            createConversationMembersTable();
        }
        truncateTable();
    }

    @Test
    @DisplayName("save + findById: should persist and read back (and set joinedAt if missing)")
    void save_thenFindById() {
        UUID id = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ConversationMember m = new ConversationMember();
        m.setId(id);
        m.setConversationId(conversationId);
        m.setUserId(userId);
        m.setJoinedAt(null);

        ConversationMember saved = repo.save(m);

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getJoinedAt()).isNotNull();

        Optional<ConversationMember> loaded = repo.findById(id);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getConversationId()).isEqualTo(conversationId);
        assertThat(loaded.get().getUserId()).isEqualTo(userId);
        assertThat(loaded.get().getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByConversationId: should return members from conversation-index GSI")
    void findByConversationId() {
        UUID conversationId = UUID.randomUUID();
        UUID otherConversationId = UUID.randomUUID();

        ConversationMember a = member(UUID.randomUUID(), conversationId, UUID.randomUUID());
        ConversationMember b = member(UUID.randomUUID(), conversationId, UUID.randomUUID());
        ConversationMember c = member(UUID.randomUUID(), otherConversationId, UUID.randomUUID());

        repo.save(a);
        repo.save(b);
        repo.save(c);

        List<ConversationMember> res = repo.findByConversationId(conversationId);
        assertThat(res).hasSize(2);
        assertThat(res).extracting(ConversationMember::getConversationId).containsOnly(conversationId);
    }

    @Test
    @DisplayName("findByUserId: should return memberships from user-index GSI")
    void findByUserId() {
        UUID userId = UUID.randomUUID();

        ConversationMember a = member(UUID.randomUUID(), UUID.randomUUID(), userId);
        ConversationMember b = member(UUID.randomUUID(), UUID.randomUUID(), userId);
        ConversationMember c = member(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        repo.save(a);
        repo.save(b);
        repo.save(c);

        List<ConversationMember> res = repo.findByUserId(userId);
        assertThat(res).hasSize(2);
        assertThat(res).extracting(ConversationMember::getUserId).containsOnly(userId);
    }

    @Test
    @DisplayName("findByConversationIdAndUserId: should return one item from conversation-user-index GSI")
    void findByConversationIdAndUserId() {
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ConversationMember a = member(UUID.randomUUID(), conversationId, userId);
        ConversationMember b = member(UUID.randomUUID(), conversationId, UUID.randomUUID());

        repo.save(a);
        repo.save(b);

        Optional<ConversationMember> res = repo.findByConversationIdAndUserId(conversationId, userId);
        assertThat(res).isPresent();
        assertThat(res.get().getConversationId()).isEqualTo(conversationId);
        assertThat(res.get().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("deleteById: should remove item")
    void deleteById() {
        UUID id = UUID.randomUUID();
        ConversationMember a = member(id, UUID.randomUUID(), UUID.randomUUID());
        repo.save(a);

        assertThat(repo.findById(id)).isPresent();

        repo.deleteById(id);

        assertThat(repo.findById(id)).isEmpty();
    }

    private ConversationMember member(UUID id, UUID conversationId, UUID userId) {
        ConversationMember m = new ConversationMember();
        m.setId(id);
        m.setConversationId(conversationId);
        m.setUserId(userId);
        m.setJoinedAt(Instant.now());
        return m;
    }

    private boolean tableExists(String tableName) {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private void createConversationMembersTable() {
        // PK: id (String)
        // GSIs:
        // - conversation-index: conversationId (String)
        // - user-index: userId (String)
        // - conversation-user-index: conversationId (String) + userId (String)
        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("conversationId").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("userId").attributeType(ScalarAttributeType.S).build()
                )
                .keySchema(
                        KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build()
                )
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(CONVERSATION_INDEX)
                                .keySchema(KeySchemaElement.builder().attributeName("conversationId").keyType(KeyType.HASH).build())
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build(),
                        GlobalSecondaryIndex.builder()
                                .indexName(USER_INDEX)
                                .keySchema(KeySchemaElement.builder().attributeName("userId").keyType(KeyType.HASH).build())
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build(),
                        GlobalSecondaryIndex.builder()
                                .indexName(CONVERSATION_USER_INDEX)
                                .keySchema(
                                        KeySchemaElement.builder().attributeName("conversationId").keyType(KeyType.HASH).build(),
                                        KeySchemaElement.builder().attributeName("userId").keyType(KeyType.RANGE).build()
                                )
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build()
                )
                .build());

        dynamoDbClient.waiter().waitUntilTableExists(DescribeTableRequest.builder().tableName(TABLE_NAME).build());
    }

    private void truncateTable() {
        // Fast/simple cleanup for small test data: scan + batch delete
        ScanResponse scan = dynamoDbClient.scan(ScanRequest.builder().tableName(TABLE_NAME).build());
        if (scan.items().isEmpty()) return;

        for (var item : scan.items()) {
            var key = java.util.Map.of("id", item.get("id"));
            dynamoDbClient.deleteItem(DeleteItemRequest.builder().tableName(TABLE_NAME).key(key).build());
        }
    }
}
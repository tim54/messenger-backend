package com.example.messenger.config;

import com.example.messenger.repo.dynamodb.model.*;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Slf4j
@Component
@Profile("dynamodb")
public class DynamoDbTableInitializer {

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${aws.dynamodb.auto-create-tables:false}")
    private boolean autoCreateTables;

    public DynamoDbTableInitializer(DynamoDbClient dynamoDbClient,
                                    DynamoDbEnhancedClient enhancedClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.enhancedClient = enhancedClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeTables() {

        if (!autoCreateTables) {
            log.info("Auto-creation of DynamoDB tables is disabled");
            return;
        }

        log.info("Initializing DynamoDB tables...");
        createUsersTable();
        createConversationsTable();
        createMessagesTable();
        createConversationMembersTable();
        createCallSessionsTable();

        log.info("DynamoDB tables initialized successfully");
    }

    private void createUsersTable() {
        String tableName = "Users";

        if (tableExists(tableName)) {
            log.info("Table {} already exists, skipping creation", tableName);
            return;
        }

        try {
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("id")
                                    .keyType(KeyType.HASH)
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("id")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("username")
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .globalSecondaryIndexes(
                            GlobalSecondaryIndex.builder()
                                    .indexName("username-index")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("username")
                                                    .keyType(KeyType.HASH)
                                                    .build()
                                    )
                                    .projection(Projection.builder()
                                            .projectionType(ProjectionType.ALL)
                                            .build())
                                    .provisionedThroughput(ProvisionedThroughput.builder()
                                            .readCapacityUnits(5L)
                                            .writeCapacityUnits(5L)
                                            .build())
                                    .build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .build();

            dynamoDbClient.createTable(request);
            waitForTableToBeActive(tableName);
            log.info("Created table: {}", tableName);
        } catch (Exception e) {
            log.error("Error creating table {}: {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to create Users table", e);
        }
    }

    private void createConversationsTable() {
        String tableName = "Conversations";

        if (tableExists(tableName)) {
            log.info("Table {} already exists, skipping creation", tableName);
            return;
        }

        try {
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("id")
                                    .keyType(KeyType.HASH)
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("id")
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .build();

            dynamoDbClient.createTable(request);
            waitForTableToBeActive(tableName);
            log.info("Created table: {}", tableName);
        } catch (Exception e) {
            log.error("Error creating table {}: {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to create Conversations table", e);
        }
    }

    private void createMessagesTable() {
        String tableName = "Messages";

        if (tableExists(tableName)) {
            log.info("Table {} already exists, skipping creation", tableName);
            return;
        }

        try {
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("id")
                                    .keyType(KeyType.HASH)
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("id")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("conversationId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("createdAt")
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .globalSecondaryIndexes(
                            GlobalSecondaryIndex.builder()
                                    .indexName("conversation-created-index")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("conversationId")
                                                    .keyType(KeyType.HASH)
                                                    .build(),
                                            KeySchemaElement.builder()
                                                    .attributeName("createdAt")
                                                    .keyType(KeyType.RANGE)
                                                    .build()
                                    )
                                    .projection(Projection.builder()
                                            .projectionType(ProjectionType.ALL)
                                            .build())
                                    .provisionedThroughput(ProvisionedThroughput.builder()
                                            .readCapacityUnits(5L)
                                            .writeCapacityUnits(5L)
                                            .build())
                                    .build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .build();

            dynamoDbClient.createTable(request);
            waitForTableToBeActive(tableName);
            log.info("Created table: {}", tableName);
        } catch (Exception e) {
            log.error("Error creating table {}: {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to create Messages table", e);
        }
    }

    private void createConversationMembersTable() {
        String tableName = "ConversationMembers";

        if (tableExists(tableName)) {
            log.info("Table {} already exists, skipping creation", tableName);
            return;
        }

        try {
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("id")
                                    .keyType(KeyType.HASH)
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("id")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("conversationId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("userId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .globalSecondaryIndexes(
                            GlobalSecondaryIndex.builder()
                                    .indexName("conversation-index")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("conversationId")
                                                    .keyType(KeyType.HASH)
                                                    .build()
                                    )
                                    .projection(Projection.builder()
                                            .projectionType(ProjectionType.ALL)
                                            .build())
                                    .provisionedThroughput(ProvisionedThroughput.builder()
                                            .readCapacityUnits(5L)
                                            .writeCapacityUnits(5L)
                                            .build())
                                    .build(),
                            GlobalSecondaryIndex.builder()
                                    .indexName("user-index")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("userId")
                                                    .keyType(KeyType.HASH)
                                                    .build()
                                    )
                                    .projection(Projection.builder()
                                            .projectionType(ProjectionType.ALL)
                                            .build())
                                    .provisionedThroughput(ProvisionedThroughput.builder()
                                            .readCapacityUnits(5L)
                                            .writeCapacityUnits(5L)
                                            .build())
                                    .build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .build();

            dynamoDbClient.createTable(request);
            waitForTableToBeActive(tableName);
            log.info("Created table: {}", tableName);
        } catch (Exception e) {
            log.error("Error creating table {}: {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to create ConversationMembers table", e);
        }
    }

    private void createCallSessionsTable() {
        String tableName = "CallSessions";

        if (tableExists(tableName)) {
            log.info("Table {} already exists, skipping creation", tableName);
            return;
        }

        try {
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("id")
                                    .keyType(KeyType.HASH)
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("id")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("conversationId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .globalSecondaryIndexes(
                            GlobalSecondaryIndex.builder()
                                    .indexName("conversation-index")
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("conversationId")
                                                    .keyType(KeyType.HASH)
                                                    .build()
                                    )
                                    .projection(Projection.builder()
                                            .projectionType(ProjectionType.ALL)
                                            .build())
                                    .provisionedThroughput(ProvisionedThroughput.builder()
                                            .readCapacityUnits(5L)
                                            .writeCapacityUnits(5L)
                                            .build())
                                    .build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .build();

            dynamoDbClient.createTable(request);
            waitForTableToBeActive(tableName);
            log.info("Created table: {}", tableName);
        } catch (Exception e) {
            log.error("Error creating table {}: {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to create CallSessions table", e);
        }
    }

    private boolean tableExists(String tableName) {
        try {
            DescribeTableRequest request = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();
            dynamoDbClient.describeTable(request);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private void waitForTableToBeActive(String tableName) {
        log.info("Waiting for table {} to become active...", tableName);

        try {
            WaiterResponse<DescribeTableResponse> waiterResponse = dynamoDbClient.waiter()
                    .waitUntilTableExists(
                            DescribeTableRequest.builder()
                                    .tableName(tableName)
                                    .build()
                    );

            waiterResponse.matched().response().ifPresent(response -> {
                log.info("Table {} is now active", tableName);
            });
        } catch (Exception e) {
            log.error("Error waiting for table {}: {}", tableName, e.getMessage());
        }
    }
}
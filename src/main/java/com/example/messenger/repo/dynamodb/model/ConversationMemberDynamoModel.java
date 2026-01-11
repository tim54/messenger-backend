package com.example.messenger.repo.dynamodb.model;

import java.time.Instant;

import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.InstantAsStringAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class ConversationMemberDynamoModel {
    private String id;
    private String conversationId;
    private String userId;
    private Instant joinedAt;
    private String lastReadMessageId;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // For querying all members in a conversation and for (conversationId, userId) lookup
    @DynamoDbSecondaryPartitionKey(indexNames = {"conversation-index", "conversation-user-index"})
    @DynamoDbAttribute("conversationId")
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    // For querying all memberships of a user, and for (conversationId, userId) lookup (sort key)
    @DynamoDbSecondaryPartitionKey(indexNames = "user-index")
    @DynamoDbSecondarySortKey(indexNames = "conversation-user-index")
    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDbConvertedBy(InstantAsStringAttributeConverter.class)
    @DynamoDbAttribute("joinedAt")
    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    @DynamoDbAttribute("lastReadMessageId")
    public String getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(String lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}
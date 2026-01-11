package com.example.messenger.repo.dynamodb.mapper;

import com.example.messenger.domain.Conversation;
import com.example.messenger.repo.dynamodb.model.ConversationDynamoModel;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {

    public ConversationDynamoModel toModel(Conversation conversation) {
        ConversationDynamoModel model = new ConversationDynamoModel();

        model.setId(
                conversation.getId() != null ? conversation.getId().toString() : UUID.randomUUID().toString());
        model.setDirect(conversation.isDirect());
        model.setCreatedAt(conversation.getCreatedAt() != null ? conversation.getCreatedAt() : Instant.now());

        return model;
    }

    public Conversation toDomain(ConversationDynamoModel model) {
        Conversation conversation = new Conversation();

        conversation.setId(model.getId() != null ? UUID.fromString(model.getId()) : null);
        conversation.setDirect(Boolean.TRUE.equals(model.getDirect()));
        conversation.setCreatedAt(model.getCreatedAt());

        return conversation;
    }
}
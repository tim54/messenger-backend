package com.example.messenger.repo.dynamodb.mapper;

import com.example.messenger.domain.Message;
import com.example.messenger.repo.dynamodb.model.MessageDynamoModel;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageDynamoModel toModel(Message message) {
        MessageDynamoModel model = new MessageDynamoModel();
        model.setId(message.getId() != null ? message.getId().toString() : UUID.randomUUID().toString());
        model.setConversationId(message.getConversationId() != null ? message.getConversationId().toString() : null);
        model.setSenderId(message.getSenderId() != null ? message.getSenderId().toString() : null);
        model.setContent(message.getContent());
        model.setCreatedAt(message.getCreatedAt());
        model.setEditedAt(message.getEditedAt());
        return model;
    }

    public Message toDomain(MessageDynamoModel model) {
        Message message = new Message();
        message.setId(model.getId() != null ? UUID.fromString(model.getId()) : null);
        message.setConversationId(
                model.getConversationId() != null ? UUID.fromString(model.getConversationId()) : null);
        message.setSenderId(model.getSenderId() != null ? UUID.fromString(model.getSenderId()) : null);
        message.setContent(model.getContent());
        message.setCreatedAt(model.getCreatedAt());
        message.setEditedAt(model.getEditedAt());
        return message;
    }
}
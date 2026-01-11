package com.example.messenger.repo.dynamodb.mapper;

import com.example.messenger.domain.ConversationMember;
import com.example.messenger.repo.dynamodb.model.ConversationMemberDynamoModel;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConversationMemberMapper {

    public ConversationMemberDynamoModel toModel(ConversationMember member) {
        ConversationMemberDynamoModel model = new ConversationMemberDynamoModel();

        model.setId(member.getId() != null ? member.getId().toString() : UUID.randomUUID().toString());
        model.setConversationId(
                member.getConversationId() != null ? member.getConversationId().toString() : null);
        model.setUserId(member.getUserId() != null ? member.getUserId().toString() : null);

        model.setJoinedAt(member.getJoinedAt() != null ? member.getJoinedAt() : Instant.now());
        model.setLastReadMessageId(
                member.getLastReadMessageId() != null ? member.getLastReadMessageId().toString() : null);

        return model;
    }

    public ConversationMember toDomain(ConversationMemberDynamoModel model) {
        ConversationMember member = new ConversationMember();

        member.setId(model.getId() != null ? UUID.fromString(model.getId()) : null);
        member.setConversationId(
                model.getConversationId() != null ? UUID.fromString(model.getConversationId()) : null);
        member.setUserId(model.getUserId() != null ? UUID.fromString(model.getUserId()) : null);

        member.setJoinedAt(model.getJoinedAt());
        member.setLastReadMessageId(
                model.getLastReadMessageId() != null ? UUID.fromString(model.getLastReadMessageId()) : null);

        return member;
    }
}
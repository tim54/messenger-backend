
package com.example.messenger.service;

import com.example.messenger.domain.Message;
import com.example.messenger.repo.MessageRepository;
import com.example.messenger.repo.api.GenericMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {
    private final GenericMessageRepository messageRepository;

    public MessageService(GenericMessageRepository messageRepository) { this.messageRepository = messageRepository; }

    public Message send(UUID conversationId, UUID senderId, String content) {
        Message m = new Message();
        m.setConversationId(conversationId);
        m.setSenderId(senderId);
        m.setContent(content);
        return messageRepository.save(m);
    }

    public List<Message> history(UUID conversationId, int limit) {
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(0, limit));
    }
}

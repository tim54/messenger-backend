
package com.example.messenger.api;

import com.example.messenger.api.dto.MessageDtos;
import com.example.messenger.domain.Message;
import com.example.messenger.repo.UserRepository;
import com.example.messenger.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MessageController {
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageService messageService, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/conversations/{id}/messages")
    public MessageDtos.MessageResponse send(@PathVariable("id") UUID conversationId,
                                            @Valid @RequestBody MessageDtos.SendMessageRequest req,
                                            @AuthenticationPrincipal User principal) {
        var me = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        Message m = messageService.send(conversationId, me.getId(), req.content());
        var resp = new MessageDtos.MessageResponse(m.getId(), m.getConversationId(), m.getSenderId(), m.getContent(), m.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/chat." + conversationId, resp);
        return resp;
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageDtos.MessageResponse> history(@PathVariable("id") UUID conversationId, @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return messageService.history(conversationId, Math.min(200, Math.max(1, limit)))
                .stream().map(m -> new MessageDtos.MessageResponse(m.getId(), m.getConversationId(), m.getSenderId(), m.getContent(), m.getCreatedAt()))
                .toList();
    }
}


package com.example.messenger.api;

import com.example.messenger.api.dto.ConversationDtos;
import com.example.messenger.domain.Conversation;
import com.example.messenger.repo.UserRepository;
import com.example.messenger.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final UserRepository userRepository;

    public ConversationController(ConversationService conversationService, UserRepository userRepository) {
        this.conversationService = conversationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ConversationDtos.ConversationResponse create(@Valid @RequestBody ConversationDtos.CreateConversationRequest req,
                                                        @AuthenticationPrincipal User principal) {
        var me = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        List<UUID> members = req.memberIds();
        if (!members.contains(me.getId())) members = new ArrayList<>(members) {{ add(me.getId()); }};
        Conversation c = conversationService.create(req.direct(), members);
        return new ConversationDtos.ConversationResponse(c.getId(), c.isDirect());
    }

    @GetMapping
    public List<?> myConversations(@AuthenticationPrincipal User principal) {
        var me = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return conversationService.listMemberships(me.getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID conversationId, @AuthenticationPrincipal User principal) {
        var me = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        conversationService.delete(conversationId);
    }
}

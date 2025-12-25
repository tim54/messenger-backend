
package com.example.messenger.service;

import com.example.messenger.api.dto.ConversationDtos;
import com.example.messenger.domain.Conversation;
import com.example.messenger.domain.ConversationMember;
import com.example.messenger.repo.ConversationMemberRepository;
import com.example.messenger.repo.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;

    public ConversationService(ConversationRepository conversationRepository, ConversationMemberRepository memberRepository) {
        this.conversationRepository = conversationRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Conversation create(boolean direct, List<UUID> memberIds) {
        Conversation c = new Conversation();
        c.setDirect(direct);
        conversationRepository.save(c);
        for (UUID uid : memberIds) {
            ConversationMember m = new ConversationMember();
            m.setConversationId(c.getId());
            m.setUserId(uid);
            memberRepository.save(m);
        }
        return c;
    }

    public List<ConversationMember> listMemberships(UUID userId) {
        return memberRepository.findByUserId(userId);
    }

    @Transactional
    public void delete(UUID conversationId) {
        memberRepository.deleteByConversationId(conversationId);
        conversationRepository.deleteById(conversationId);
    }

}

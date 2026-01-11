
package com.example.messenger.service;

import com.example.messenger.api.dto.ConversationDtos;
import com.example.messenger.domain.Conversation;
import com.example.messenger.domain.ConversationMember;
import com.example.messenger.repo.ConversationMemberRepository;
import com.example.messenger.repo.ConversationRepository;
import com.example.messenger.repo.api.GenericConversationMemberRepository;
import com.example.messenger.repo.api.GenericConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {
    private final GenericConversationRepository conversationRepository;
    private final GenericConversationMemberRepository memberRepository;

    public ConversationService(GenericConversationRepository conversationRepository, GenericConversationMemberRepository memberRepository) {
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
        memberRepository.deleteById(conversationId);
        conversationRepository.deleteById(conversationId);
    }

}

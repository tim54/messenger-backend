package com.example.messenger.controller;

import com.example.messenger.api.MessageController;
import com.example.messenger.api.dto.MessageDtos;
import com.example.messenger.domain.Message;
import com.example.messenger.repo.api.GenericUserRepository;
import com.example.messenger.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.User;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageController Tests")
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private GenericUserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageController messageController;

    private User principal;
    private com.example.messenger.domain.User domainUser;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    @BeforeEach
    void setUp() {
        principal = new User("testuser", "pw", Collections.emptyList());

        domainUser = new com.example.messenger.domain.User();
        domainUser.setId(UUID.randomUUID());
        domainUser.setUsername("testuser");
    }

    @Test
    @DisplayName("send() should delegate to service, publish websocket message, and return DTO")
    void send_shouldDelegatePublishAndReturnDto() {
        // Given
        UUID conversationId = UUID.randomUUID();
        MessageDtos.SendMessageRequest req = mock(MessageDtos.SendMessageRequest.class);
        when(req.content()).thenReturn("hello");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));

        Message saved = new Message();
        UUID messageId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        saved.setId(messageId);
        saved.setConversationId(conversationId);
        saved.setSenderId(domainUser.getId());
        saved.setContent("hello");
        saved.setCreatedAt(createdAt);

        when(messageService.send(conversationId, domainUser.getId(), "hello")).thenReturn(saved);

        // When
        MessageDtos.MessageResponse res = messageController.send(conversationId, req, principal);

        // Then
        assertThat(res).isNotNull();
        assertThat(res.id()).isEqualTo(messageId);
        assertThat(res.conversationId()).isEqualTo(conversationId);
        assertThat(res.senderId()).isEqualTo(domainUser.getId());
        assertThat(res.content()).isEqualTo("hello");
        assertThat(res.createdAt()).isEqualTo(createdAt);

        verify(userRepository).findByUsername("testuser");
        verify(messageService).send(conversationId, domainUser.getId(), "hello");

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/chat." + conversationId);
        assertThat(payloadCaptor.getValue()).isInstanceOf(MessageDtos.MessageResponse.class);

        MessageDtos.MessageResponse pushed = (MessageDtos.MessageResponse) payloadCaptor.getValue();
        assertThat(pushed.id()).isEqualTo(messageId);
        assertThat(pushed.conversationId()).isEqualTo(conversationId);

        verifyNoMoreInteractions(userRepository, messageService, messagingTemplate);
    }

    @Test
    @DisplayName("send() should throw when user not found")
    void send_whenUserNotFound_shouldThrow() {
        // Given
        UUID conversationId = UUID.randomUUID();
        MessageDtos.SendMessageRequest req = mock(MessageDtos.SendMessageRequest.class);
        when(req.content()).thenReturn("hello");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> messageController.send(conversationId, req, principal))
                .isInstanceOf(java.util.NoSuchElementException.class);

        verify(userRepository).findByUsername("testuser");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(messageService, messagingTemplate);
    }

    @Test
    @DisplayName("history() should clamp limit to [1..200], delegate to service, and map DTOs")
    void history_shouldClampLimitDelegateAndMap() {
        // Given
        UUID conversationId = UUID.randomUUID();

        Message m1 = new Message();
        m1.setId(UUID.randomUUID());
        m1.setConversationId(conversationId);
        m1.setSenderId(UUID.randomUUID());
        m1.setContent("c1");
        m1.setCreatedAt(Instant.now());

        Message m2 = new Message();
        m2.setId(UUID.randomUUID());
        m2.setConversationId(conversationId);
        m2.setSenderId(UUID.randomUUID());
        m2.setContent("c2");
        m2.setCreatedAt(Instant.now());

        when(messageService.history(conversationId, 1)).thenReturn(List.of(m1, m2));

        // When: limit < 1 should be clamped to 1
        List<MessageDtos.MessageResponse> res = messageController.history(conversationId, 0);

        // Then
        assertThat(res).hasSize(2);
        assertThat(res.get(0).id()).isEqualTo(m1.getId());
        assertThat(res.get(0).conversationId()).isEqualTo(conversationId);
        assertThat(res.get(0).senderId()).isEqualTo(m1.getSenderId());
        assertThat(res.get(0).content()).isEqualTo("c1");

        verify(messageService).history(conversationId, 1);
        verifyNoMoreInteractions(messageService);
        verifyNoInteractions(userRepository, messagingTemplate);
    }

    @Test
    @DisplayName("history() should clamp limit > 200 down to 200")
    void history_whenLimitTooHigh_shouldClampTo200() {
        // Given
        UUID conversationId = UUID.randomUUID();
        when(messageService.history(conversationId, 200)).thenReturn(List.of());

        // When
        List<MessageDtos.MessageResponse> res = messageController.history(conversationId, 999);

        // Then
        assertThat(res).isEmpty();
        verify(messageService).history(conversationId, 200);
        verifyNoMoreInteractions(messageService);
        verifyNoInteractions(userRepository, messagingTemplate);
    }
}

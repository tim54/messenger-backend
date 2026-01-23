
package service;

import com.example.messenger.domain.Message;
import com.example.messenger.repo.api.GenericMessageRepository;
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
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Tests")
class MessageServiceTest {

    @Mock
    private GenericMessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    @Captor
    ArgumentCaptor<Message> messageCaptor;

    private UUID conversationId;
    private UUID senderId;
    private String content;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        content = "Test message content";
    }

    @Test
    @DisplayName("Should send message successfully")
    void testSend_Success() {
        // Arrange
        Message expectedMessage = new Message();
        expectedMessage.setConversationId(conversationId);
        expectedMessage.setSenderId(senderId);
        expectedMessage.setContent(content);

        when(messageRepository.save(any(Message.class))).thenReturn(expectedMessage);

        // Act
        Message result = messageService.send(conversationId, senderId, content);

        // Assert
        assertNotNull(result);
        assertEquals(conversationId, result.getConversationId());
        assertEquals(senderId, result.getSenderId());
        assertEquals(content, result.getContent());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Should capture and verify message properties when sending")
    void testSend_VerifyMessageProperties() {
        // Arrange
        Message savedMessage = new Message();
        savedMessage.setConversationId(conversationId);
        savedMessage.setSenderId(senderId);
        savedMessage.setContent(content);

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // Act
        messageService.send(conversationId, senderId, content);

        // Assert
        verify(messageRepository).save(messageCaptor.capture());
        Message capturedMessage = messageCaptor.getValue();
        assertEquals(conversationId, capturedMessage.getConversationId());
        assertEquals(senderId, capturedMessage.getSenderId());
        assertEquals(content, capturedMessage.getContent());
    }

    @Test
    @DisplayName("Should send message with empty content")
    void testSend_EmptyContent() {
        // Arrange
        String emptyContent = "";
        Message expectedMessage = new Message();
        expectedMessage.setConversationId(conversationId);
        expectedMessage.setSenderId(senderId);
        expectedMessage.setContent(emptyContent);

        when(messageRepository.save(any(Message.class))).thenReturn(expectedMessage);

        // Act
        Message result = messageService.send(conversationId, senderId, emptyContent);

        // Assert
        assertNotNull(result);
        assertEquals(emptyContent, result.getContent());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Should retrieve message history successfully")
    void testHistory_Success() {
        // Arrange
        int limit = 10;
        Message message1 = createMessage(conversationId, senderId, "Message 1");
        Message message2 = createMessage(conversationId, senderId, "Message 2");
        Message message3 = createMessage(conversationId, senderId, "Message 3");
        List<Message> expectedMessages = Arrays.asList(message1, message2, message3);

        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                any(PageRequest.class)
        )).thenReturn(expectedMessages);

        // Act
        List<Message> result = messageService.history(conversationId, limit);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedMessages, result);
        verify(messageRepository, times(1)).findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                eq(PageRequest.of(0, limit))
        );
    }

    @Test
    @DisplayName("Should retrieve empty history when no messages exist")
    void testHistory_EmptyResult() {
        // Arrange
        int limit = 10;
        List<Message> emptyList = List.of();

        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                any(PageRequest.class)
        )).thenReturn(emptyList);

        // Act
        List<Message> result = messageService.history(conversationId, limit);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(messageRepository, times(1)).findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                eq(PageRequest.of(0, limit))
        );
    }

    @Test
    @DisplayName("Should retrieve history with custom limit")
    void testHistory_CustomLimit() {
        // Arrange
        int limit = 5;
        List<Message> expectedMessages = Arrays.asList(
                createMessage(conversationId, senderId, "Message 1"),
                createMessage(conversationId, senderId, "Message 2")
        );

        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                any(PageRequest.class)
        )).thenReturn(expectedMessages);

        // Act
        List<Message> result = messageService.history(conversationId, limit);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageRepository, times(1)).findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                eq(PageRequest.of(0, limit))
        );
    }

    @Test
    @DisplayName("Should handle limit of 1")
    void testHistory_LimitOne() {
        // Arrange
        int limit = 1;
        Message message = createMessage(conversationId, senderId, "Single message");
        List<Message> expectedMessages = List.of(message);

        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                any(PageRequest.class)
        )).thenReturn(expectedMessages);

        // Act
        List<Message> result = messageService.history(conversationId, limit);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Single message", result.get(0).getContent());
        verify(messageRepository, times(1)).findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                eq(PageRequest.of(0, 1))
        );
    }

    @Test
    @DisplayName("Should use correct PageRequest parameters")
    void testHistory_VerifyPageRequestParameters() {
        // Arrange
        int limit = 20;
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);

        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                any(PageRequest.class)
        )).thenReturn(List.of());

        // Act
        messageService.history(conversationId, limit);

        // Assert
        verify(messageRepository).findByConversationIdOrderByCreatedAtDesc(
                eq(conversationId), 
                pageRequestCaptor.capture()
        );
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(0, capturedPageRequest.getPageNumber());
        assertEquals(limit, capturedPageRequest.getPageSize());
    }

    // Helper method
    private Message createMessage(UUID conversationId, UUID senderId, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        return message;
    }
}

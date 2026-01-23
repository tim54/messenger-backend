package controller;

import com.example.messenger.api.ConversationController;
import com.example.messenger.api.dto.ConversationDtos;
import com.example.messenger.domain.Conversation;
import com.example.messenger.domain.ConversationMember;
import com.example.messenger.repo.api.GenericUserRepository;
import com.example.messenger.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationController Tests")
class ConversationControllerTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private GenericUserRepository userRepository;

    @InjectMocks
    private ConversationController conversationController;

    @Captor
    private ArgumentCaptor<List<UUID>> memberIdsCaptor;

    private User principal;
    private com.example.messenger.domain.User domainUser;
    private UUID userId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
        
        principal = new User("testuser", "password", Collections.emptyList());
        
        domainUser = new com.example.messenger.domain.User();
        domainUser.setId(userId);
        domainUser.setUsername("testuser");
        domainUser.setDisplayName("Test User");
        domainUser.setPasswordHash("hashed");
        domainUser.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should create direct conversation with provided members")
    void testCreate_WhenDirectConversationWithMembers_ShouldCreateAndReturnResponse() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        ConversationDtos.CreateConversationRequest request = 
                new ConversationDtos.CreateConversationRequest(true, List.of(otherUserId));
        
        Conversation mockConversation = createConversation(conversationId, true);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));
        when(conversationService.create(eq(true), anyList())).thenReturn(mockConversation);

        // When
        ConversationDtos.ConversationResponse response = conversationController.create(request, principal);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(conversationId);
        assertThat(response.direct()).isTrue();
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(conversationService, times(1)).create(eq(true), memberIdsCaptor.capture());
        
        List<UUID> capturedMemberIds = memberIdsCaptor.getValue();
        assertThat(capturedMemberIds).containsExactlyInAnyOrder(userId, otherUserId);
    }

    @Test
    @DisplayName("Should add current user to members if not included")
    void testCreate_WhenCurrentUserNotInMembers_ShouldAddCurrentUser() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        ConversationDtos.CreateConversationRequest request = 
                new ConversationDtos.CreateConversationRequest(false, List.of(otherUserId));
        
        Conversation mockConversation = createConversation(conversationId, false);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));
        when(conversationService.create(eq(false), anyList())).thenReturn(mockConversation);

        // When
        conversationController.create(request, principal);

        // Then
        verify(conversationService).create(eq(false), memberIdsCaptor.capture());
        List<UUID> capturedMemberIds = memberIdsCaptor.getValue();
        assertThat(capturedMemberIds).contains(userId, otherUserId);
        assertThat(capturedMemberIds).hasSize(2);
    }

    @Test
    @DisplayName("Should not duplicate current user if already in members")
    void testCreate_WhenCurrentUserAlreadyInMembers_ShouldNotDuplicate() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        ConversationDtos.CreateConversationRequest request = 
                new ConversationDtos.CreateConversationRequest(true, Arrays.asList(userId, otherUserId));
        
        Conversation mockConversation = createConversation(conversationId, true);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));
        when(conversationService.create(eq(true), anyList())).thenReturn(mockConversation);

        // When
        conversationController.create(request, principal);

        // Then
        verify(conversationService).create(eq(true), memberIdsCaptor.capture());
        List<UUID> capturedMemberIds = memberIdsCaptor.getValue();
        assertThat(capturedMemberIds).containsExactlyInAnyOrder(userId, otherUserId);
        assertThat(capturedMemberIds.stream().filter(id -> id.equals(userId)).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create group conversation with multiple members")
    void testCreate_WhenGroupConversationWithMultipleMembers_ShouldCreateSuccessfully() {
        // Given
        UUID member2 = UUID.randomUUID();
        UUID member3 = UUID.randomUUID();
        ConversationDtos.CreateConversationRequest request = 
                new ConversationDtos.CreateConversationRequest(false, Arrays.asList(member2, member3));
        
        Conversation mockConversation = createConversation(conversationId, false);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));
        when(conversationService.create(eq(false), anyList())).thenReturn(mockConversation);

        // When
        ConversationDtos.ConversationResponse response = conversationController.create(request, principal);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.direct()).isFalse();
        
        verify(conversationService).create(eq(false), memberIdsCaptor.capture());
        List<UUID> capturedMemberIds = memberIdsCaptor.getValue();
        assertThat(capturedMemberIds).hasSize(3);
        assertThat(capturedMemberIds).contains(userId, member2, member3);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testCreate_WhenUserNotFound_ShouldThrowException() {
        // Given
        ConversationDtos.CreateConversationRequest request = 
                new ConversationDtos.CreateConversationRequest(true, List.of(UUID.randomUUID()));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> conversationController.create(request, principal))
                .isInstanceOf(NoSuchElementException.class);
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(conversationService, never()).create(anyBoolean(), anyList());
    }

    @Test
    @DisplayName("Should list all conversations for user")
    void testMyConversations_WhenUserHasConversations_ShouldReturnMemberships() {
        // Given
        ConversationMember member1 = createConversationMember(UUID.randomUUID(), userId);
        ConversationMember member2 = createConversationMember(UUID.randomUUID(), userId);
        List<ConversationMember> memberships = Arrays.asList(member1, member2);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));
        when(conversationService.listMemberships(userId)).thenReturn(memberships);

        // When
        List<?> result = conversationController.myConversations(principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(memberships);
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(conversationService, times(1)).listMemberships(userId);
    }

    @Test
    @DisplayName("Should return empty list when user has no conversations")
    void testMyConversations_WhenUserHasNoConversations_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));
        when(conversationService.listMemberships(userId)).thenReturn(Collections.emptyList());

        // When
        List<?> result = conversationController.myConversations(principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(conversationService, times(1)).listMemberships(userId);
    }

    @Test
    @DisplayName("Should throw exception when listing conversations for non-existent user")
    void testMyConversations_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> conversationController.myConversations(principal))
                .isInstanceOf(NoSuchElementException.class);
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(conversationService, never()).listMemberships(any());
    }

    @Test
    @DisplayName("Should delete conversation successfully")
    void testDelete_WhenConversationExists_ShouldDeleteConversation() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));
        doNothing().when(conversationService).delete(conversationId);

        // When
        conversationController.delete(conversationId, principal);

        // Then
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(conversationService, times(1)).delete(conversationId);
    }

    @Test
    @DisplayName("Should throw exception when deleting conversation for non-existent user")
    void testDelete_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> conversationController.delete(conversationId, principal))
                .isInstanceOf(NoSuchElementException.class);
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(conversationService, never()).delete(any());
    }

    @Test
    @DisplayName("Should handle delete for non-existent conversation")
    void testDelete_WhenConversationDoesNotExist_ShouldCallService() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(domainUser));
        doNothing().when(conversationService).delete(nonExistentId);

        // When
        conversationController.delete(nonExistentId, principal);

        // Then
        verify(conversationService, times(1)).delete(nonExistentId);
    }

    private Conversation createConversation(UUID id, boolean isDirect) {
        Conversation conversation = new Conversation();
        conversation.setId(id);
        conversation.setDirect(isDirect);
        conversation.setCreatedAt(Instant.now());
        return conversation;
    }

    private ConversationMember createConversationMember(UUID conversationId, UUID userId) {
        ConversationMember member = new ConversationMember();
        member.setId(UUID.randomUUID());
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setJoinedAt(Instant.now());
        return member;
    }
}

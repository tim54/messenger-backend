
package com.example.messenger.repo;

import com.example.messenger.domain.Conversation;
import com.example.messenger.domain.ConversationMember;
import com.example.messenger.repo.ConversationMemberRepository;
import com.example.messenger.repo.ConversationRepository;
import com.example.messenger.repo.api.GenericConversationMemberRepository;
import com.example.messenger.repo.api.GenericConversationRepository;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationService Tests")
class ConversationServiceTest {

    @Mock
    private GenericConversationRepository conversationRepository;

    @Mock
    private GenericConversationMemberRepository memberRepository;

    @InjectMocks
    private ConversationService conversationService;

    @Captor
    private ArgumentCaptor<Conversation> conversationCaptor;

    @Captor
    private ArgumentCaptor<ConversationMember> memberCaptor;

    private UUID userId1;
    private UUID userId2;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        conversationId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create direct conversation with all members")
    void testCreate_WhenDirectConversation_ShouldCreateConversationAndMembers() {
        // Given
        List<UUID> memberIds = Arrays.asList(userId1, userId2);
        Conversation mockConversation = createConversation(conversationId, true);
        
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);
        when(memberRepository.save(any(ConversationMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Conversation result = conversationService.create(true, memberIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(conversationId);
        assertThat(result.isDirect()).isTrue();

        verify(conversationRepository, times(1)).save(conversationCaptor.capture());
        Conversation savedConversation = conversationCaptor.getValue();
        assertThat(savedConversation.isDirect()).isTrue();

        verify(memberRepository, times(2)).save(memberCaptor.capture());
        List<ConversationMember> savedMembers = memberCaptor.getAllValues();
        assertThat(savedMembers).hasSize(2);
        assertThat(savedMembers).extracting(ConversationMember::getUserId)
                .containsExactlyInAnyOrder(userId1, userId2);
        assertThat(savedMembers).extracting(ConversationMember::getConversationId)
                .containsOnly(conversationId);
    }

    @Test
    @DisplayName("Should create group conversation with all members")
    void testCreate_WhenGroupConversation_ShouldCreateConversationAndMembers() {
        // Given
        List<UUID> memberIds = Arrays.asList(userId1, userId2, UUID.randomUUID());
        Conversation mockConversation = createConversation(conversationId, false);
        
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);
        when(memberRepository.save(any(ConversationMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Conversation result = conversationService.create(false, memberIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isDirect()).isFalse();
        
        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(memberRepository, times(3)).save(any(ConversationMember.class));
    }

    @Test
    @DisplayName("Should create conversation with single member")
    void testCreate_WhenSingleMember_ShouldCreateConversationWithOneMember() {
        // Given
        List<UUID> memberIds = List.of(userId1);
        Conversation mockConversation = createConversation(conversationId, true);
        
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);
        when(memberRepository.save(any(ConversationMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Conversation result = conversationService.create(true, memberIds);

        // Then
        assertThat(result).isNotNull();
        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(memberRepository, times(1)).save(memberCaptor.capture());
        
        ConversationMember savedMember = memberCaptor.getValue();
        assertThat(savedMember.getUserId()).isEqualTo(userId1);
        assertThat(savedMember.getConversationId()).isEqualTo(conversationId);
    }

    @Test
    @DisplayName("Should create conversation with empty member list")
    void testCreate_WhenNoMembers_ShouldCreateConversationWithoutMembers() {
        // Given
        List<UUID> memberIds = List.of();
        Conversation mockConversation = createConversation(conversationId, false);
        
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);

        // When
        Conversation result = conversationService.create(false, memberIds);

        // Then
        assertThat(result).isNotNull();
        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(memberRepository, never()).save(any(ConversationMember.class));
    }

    @Test
    @DisplayName("Should list all memberships for user")
    void testListMemberships_WhenUserHasMemberships_ShouldReturnAllMemberships() {
        // Given
        ConversationMember member1 = createConversationMember(UUID.randomUUID(), userId1);
        ConversationMember member2 = createConversationMember(UUID.randomUUID(), userId1);
        List<ConversationMember> expectedMembers = Arrays.asList(member1, member2);
        
        when(memberRepository.findByUserId(userId1)).thenReturn(expectedMembers);

        // When
        List<ConversationMember> result = conversationService.listMemberships(userId1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedMembers);
        verify(memberRepository, times(1)).findByUserId(userId1);
    }

    @Test
    @DisplayName("Should return empty list when user has no memberships")
    void testListMemberships_WhenUserHasNoMemberships_ShouldReturnEmptyList() {
        // Given
        when(memberRepository.findByUserId(userId1)).thenReturn(List.of());

        // When
        List<ConversationMember> result = conversationService.listMemberships(userId1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(memberRepository, times(1)).findByUserId(userId1);
    }

    @Test
    @DisplayName("Should delete conversation and all its members")
    void testDelete_WhenConversationExists_ShouldDeleteConversationAndMembers() {
        // Given
        doNothing().when(memberRepository).deleteById(conversationId);
        doNothing().when(conversationRepository).deleteById(conversationId);

        // When
        conversationService.delete(conversationId);

        // Then
        verify(memberRepository, times(1)).deleteById(conversationId);
        verify(conversationRepository, times(1)).deleteById(conversationId);
        
        // Verify order: members are deleted before conversation
        var inOrder = inOrder(memberRepository, conversationRepository);
        inOrder.verify(memberRepository).deleteById(conversationId);
        inOrder.verify(conversationRepository).deleteById(conversationId);
    }

    @Test
    @DisplayName("Should handle delete when conversation has no members")
    void testDelete_WhenConversationHasNoMembers_ShouldStillDeleteConversation() {
        // Given
        doNothing().when(memberRepository).deleteById(conversationId);
        doNothing().when(conversationRepository).deleteById(conversationId);

        // When
        conversationService.delete(conversationId);

        // Then
        verify(memberRepository, times(1)).deleteById(conversationId);
        verify(conversationRepository, times(1)).deleteById(conversationId);
    }

    @Test
    @DisplayName("Should preserve transactional behavior in create operation")
    void testCreate_WhenTransactional_ShouldSaveConversationBeforeMembers() {
        // Given
        List<UUID> memberIds = List.of(userId1);
        Conversation mockConversation = createConversation(conversationId, true);
        
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);
        when(memberRepository.save(any(ConversationMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        conversationService.create(true, memberIds);

        // Then
        var inOrder = inOrder(conversationRepository, memberRepository);
        inOrder.verify(conversationRepository).save(any(Conversation.class));
        inOrder.verify(memberRepository).save(any(ConversationMember.class));
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

package com.example.messenger.repo;

import com.example.messenger.domain.ConversationMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles({"test", "postgres"})
@DisplayName("ConversationMemberRepository Tests")
class ConversationMemberPostgresRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    private UUID testUserId1;
    private UUID testUserId2;
    private UUID testConversationId1;
    private UUID testConversationId2;
    private ConversationMember member1;
    private ConversationMember member2;
    private ConversationMember member3;

    @BeforeEach
    void setUp() {
        testUserId1 = UUID.randomUUID();
        testUserId2 = UUID.randomUUID();
        testConversationId1 = UUID.randomUUID();
        testConversationId2 = UUID.randomUUID();

        // Create test data
        member1 = createConversationMember(testConversationId1, testUserId1);
        member2 = createConversationMember(testConversationId1, testUserId2);
        member3 = createConversationMember(testConversationId2, testUserId1);

        entityManager.persistAndFlush(member1);
        entityManager.persistAndFlush(member2);
        entityManager.persistAndFlush(member3);
    }

    @Test
    @DisplayName("Should find all members by user ID")
    void testFindByUserId_WhenUserHasMultipleMemberships_ShouldReturnAllMemberships() {
        // When
        List<ConversationMember> members = conversationMemberRepository.findByUserId(testUserId1);

        // Then
        assertThat(members).isNotNull();
        assertThat(members).hasSize(2);
        assertThat(members).extracting(ConversationMember::getUserId)
                .containsOnly(testUserId1);
        assertThat(members).extracting(ConversationMember::getConversationId)
                .containsExactlyInAnyOrder(testConversationId1, testConversationId2);
    }

    @Test
    @DisplayName("Should return empty list when user has no memberships")
    void testFindByUserId_WhenUserHasNoMemberships_ShouldReturnEmptyList() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        List<ConversationMember> members = conversationMemberRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(members).isNotNull();
        assertThat(members).isEmpty();
    }

    @Test
    @DisplayName("Should find all members by conversation ID")
    void testFindByConversationId_WhenConversationHasMultipleMembers_ShouldReturnAllMembers() {
        // When
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(testConversationId1);

        // Then
        assertThat(members).isNotNull();
        assertThat(members).hasSize(2);
        assertThat(members).extracting(ConversationMember::getConversationId)
                .containsOnly(testConversationId1);
        assertThat(members).extracting(ConversationMember::getUserId)
                .containsExactlyInAnyOrder(testUserId1, testUserId2);
    }

    @Test
    @DisplayName("Should return empty list when conversation has no members")
    void testFindByConversationId_WhenConversationHasNoMembers_ShouldReturnEmptyList() {
        // Given
        UUID nonExistentConversationId = UUID.randomUUID();

        // When
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(nonExistentConversationId);

        // Then
        assertThat(members).isNotNull();
        assertThat(members).isEmpty();
    }

    @Test
    @DisplayName("Should delete all members by conversation ID")
    void testDeleteByConversationId_WhenConversationHasMembers_ShouldDeleteAllMembers() {
        // Given
        List<ConversationMember> membersBefore = conversationMemberRepository.findByConversationId(testConversationId1);
        assertThat(membersBefore).hasSize(2);

        // When
        conversationMemberRepository.deleteByConversationId(testConversationId1);
        entityManager.flush();
        entityManager.clear();

        // Then
        List<ConversationMember> membersAfter = conversationMemberRepository.findByConversationId(testConversationId1);
        assertThat(membersAfter).isEmpty();

        // Verify other conversation members are not affected
        List<ConversationMember> otherMembers = conversationMemberRepository.findByConversationId(testConversationId2);
        assertThat(otherMembers).hasSize(1);
    }

    @Test
    @DisplayName("Should not throw exception when deleting non-existent conversation members")
    void testDeleteByConversationId_WhenConversationHasNoMembers_ShouldNotThrowException() {
        // Given
        UUID nonExistentConversationId = UUID.randomUUID();

        // When & Then
        conversationMemberRepository.deleteByConversationId(nonExistentConversationId);
        entityManager.flush();
        // No exception should be thrown
    }

    @Test
    @DisplayName("Should save and retrieve conversation member with all fields")
    void testSave_WhenValidMember_ShouldPersistAllFields() {
        // Given
        UUID lastReadMessageId = UUID.randomUUID();
        ConversationMember newMember = new ConversationMember();
        newMember.setConversationId(testConversationId2);
        newMember.setUserId(testUserId2);
        newMember.setLastReadMessageId(lastReadMessageId);

        // When
        ConversationMember savedMember = conversationMemberRepository.save(newMember);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<ConversationMember> retrievedMember = conversationMemberRepository.findById(savedMember.getId());
        assertThat(retrievedMember).isPresent();
        assertThat(retrievedMember.get().getId()).isNotNull();
        assertThat(retrievedMember.get().getConversationId()).isEqualTo(testConversationId2);
        assertThat(retrievedMember.get().getUserId()).isEqualTo(testUserId2);
        assertThat(retrievedMember.get().getJoinedAt()).isNotNull();
        assertThat(retrievedMember.get().getLastReadMessageId()).isEqualTo(lastReadMessageId);
    }

    @Test
    @DisplayName("Should update last read message ID")
    void testUpdate_WhenUpdatingLastReadMessageId_ShouldPersistChange() {
        // Given
        UUID newLastReadMessageId = UUID.randomUUID();
        ConversationMember memberToUpdate = conversationMemberRepository.findById(member1.getId()).orElseThrow();

        // When
        memberToUpdate.setLastReadMessageId(newLastReadMessageId);
        conversationMemberRepository.save(memberToUpdate);
        entityManager.flush();
        entityManager.clear();

        // Then
        ConversationMember updatedMember = conversationMemberRepository.findById(member1.getId()).orElseThrow();
        assertThat(updatedMember.getLastReadMessageId()).isEqualTo(newLastReadMessageId);
    }

    @Test
    @DisplayName("Should delete member by ID")
    void testDeleteById_WhenMemberExists_ShouldRemoveMember() {
        // Given
        UUID memberId = member1.getId();
        assertThat(conversationMemberRepository.existsById(memberId)).isTrue();

        // When
        conversationMemberRepository.deleteById(memberId);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(conversationMemberRepository.existsById(memberId)).isFalse();
    }

    @Test
    @DisplayName("Should find member by ID")
    void testFindById_WhenMemberExists_ShouldReturnMember() {
        // When
        Optional<ConversationMember> foundMember = conversationMemberRepository.findById(member1.getId());

        // Then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getId()).isEqualTo(member1.getId());
        assertThat(foundMember.get().getConversationId()).isEqualTo(member1.getConversationId());
        assertThat(foundMember.get().getUserId()).isEqualTo(member1.getUserId());
    }

    @Test
    @DisplayName("Should return empty optional when member not found by ID")
    void testFindById_WhenMemberDoesNotExist_ShouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<ConversationMember> foundMember = conversationMemberRepository.findById(nonExistentId);

        // Then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("Should count all conversation members")
    void testCount_ShouldReturnCorrectCount() {
        // When
        long count = conversationMemberRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
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

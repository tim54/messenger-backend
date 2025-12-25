
package com.example.messenger.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "call_sessions")
public class CallSession {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID conversationId;

    @Column(nullable = false)
    private UUID callerId;

    @Column(nullable = false)
    private UUID calleeId;

    @Column(nullable = false)
    private String status; // INITIATED, RINGING, ACTIVE, ENDED

    private Instant startedAt;
    private Instant endedAt;

    @PrePersist
    public void pre() {
        if (id == null) id = UUID.randomUUID();
        if (startedAt == null) startedAt = Instant.now();
        if (status == null) status = "INITIATED";
    }

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }
    public UUID getCallerId() { return callerId; }
    public void setCallerId(UUID callerId) { this.callerId = callerId; }
    public UUID getCalleeId() { return calleeId; }
    public void setCalleeId(UUID calleeId) { this.calleeId = calleeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }
}

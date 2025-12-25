
package com.example.messenger.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    private UUID id;
    @Column(nullable = false)
    private boolean isDirect;
    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    public void pre() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public boolean isDirect() { return isDirect; }
    public void setDirect(boolean direct) { isDirect = direct; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

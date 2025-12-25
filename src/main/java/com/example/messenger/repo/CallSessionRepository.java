
package com.example.messenger.repo;

import com.example.messenger.domain.CallSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CallSessionRepository extends JpaRepository<CallSession, UUID> { }

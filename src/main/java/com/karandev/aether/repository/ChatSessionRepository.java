package com.karandev.aether.repository;

import com.karandev.aether.entity.ChatSession;
import com.karandev.aether.entity.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, ChatSessionId> {
}

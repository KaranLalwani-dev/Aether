package com.karandev.aether.dto.chat;

import com.karandev.aether.entity.ChatEvent;
import com.karandev.aether.entity.ChatSession;
import com.karandev.aether.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        Long id,
        MessageRole role,
        List<ChatEventResponse> events,
        String content,
        Integer tokensUsed,
        Instant createdAt

) {
}
package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.chat.ChatResponse;
import com.karandev.aether.entity.ChatMessage;
import com.karandev.aether.entity.ChatSession;
import com.karandev.aether.entity.ChatSessionId;
import com.karandev.aether.mapper.ChatMapper;
import com.karandev.aether.repository.ChatMessageRepository;
import com.karandev.aether.repository.ChatSessionRepository;
import com.karandev.aether.security.AuthUtil;
import com.karandev.aether.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final AuthUtil authUtil;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMapper chatMapper;


    public List<ChatResponse> getProjectChatHistory(Long projectId) {
        Long userId = authUtil.getCurrentUserId();

        ChatSession chatSession = chatSessionRepository.getReferenceById(new ChatSessionId(projectId, userId));
        List<ChatMessage> chatMessageList = chatMessageRepository.findByChatSession(chatSession);
        return chatMapper.fromListOfChatMessage(chatMessageList);
    }
}

package com.karandev.aether.llm.advisors;

import com.karandev.aether.dto.project.FileNode;
import com.karandev.aether.service.ProjectFileService;
import com.karandev.aether.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileTreeContextAdvisor implements StreamAdvisor {

    private final ProjectFileService projectFileService;

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Map<String, Object> context = chatClientRequest.context();
        Long projectId = Long.parseLong(context.getOrDefault("projectId", 0L).toString());
        ChatClientRequest augmentedChatClientRequest = augmentRequestWithFileTree(chatClientRequest, projectId);

        return streamAdvisorChain.nextStream(augmentedChatClientRequest);
    }

    private ChatClientRequest augmentRequestWithFileTree(ChatClientRequest chatClientRequest, Long projectId) {
        List<Message> incomingMessages = chatClientRequest.prompt().getInstructions();

        Message systemMessage = incomingMessages.stream()
                .filter(m -> m.getMessageType() == MessageType.SYSTEM)
                .findFirst()
                .orElse(null);

        List<Message> userMessages = incomingMessages.stream()
                .filter(m -> m.getMessageType() != MessageType.SYSTEM)
                .toList();

        List<Message> allMessages = new ArrayList<>();

        if(systemMessage != null) {
            allMessages.add(systemMessage);
        }

        List<FileNode> fileTree = projectFileService.getFileTree(projectId);
        String fileTreeContext = "\n\n ---- FILE_TREE ----\n"+fileTree.toString();
        allMessages.add(new SystemMessage(fileTreeContext));

        allMessages.addAll(userMessages);

        return chatClientRequest
                .mutate()
                .prompt(new Prompt(allMessages, chatClientRequest.prompt().getOptions()))
                .build();
    }


    @Override
    public String getName() {
        return "FileTreeContextAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

package com.karandev.aether.service.Impl;

import com.karandev.aether.llm.PromptUtils;
import com.karandev.aether.llm.advisors.FileTreeContextAdvisor;
import com.karandev.aether.llm.tools.CodeGenerationTools;
import com.karandev.aether.security.AuthUtil;
import com.karandev.aether.service.AICodeGenerationService;
import com.karandev.aether.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AICodeGenerationServiceImpl implements AICodeGenerationService {

    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private final ProjectFileService projectFileService;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;

    private static final Pattern FILE_TAG_PATTERN = Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>", Pattern.DOTALL);

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<String> streamResponse(String userMessage, Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        createChatSessionIfNotExists(projectId, userId);

        Map<String, Object> advisorParams = Map.of(
                "userId", userId,
                "projectId", projectId
        );

        StringBuilder fullResponseBuffer = new StringBuilder();
        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectFileService, projectId);

        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(codeGenerationTools)
                .advisors(
                        advisorSpec -> {
                            advisorSpec.params(advisorParams);
                            advisorSpec.advisors(fileTreeContextAdvisor);
                        }
                )
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    String content = response.getResult().getOutput().getText();
                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(() -> {
                        parseAndSaveFiles(fullResponseBuffer.toString(), projectId);
                    });
                    parseAndSaveFiles(fullResponseBuffer.toString(), projectId);
                })
                .doOnError(error -> log.error("Error during streaming for projectId" + projectId))
                .filter(response -> response.getResult().getOutput().getText() != null) // will have to look into this
                .map(response -> response.getResult().getOutput().getText());
    }

    private void parseAndSaveFiles(String fullResponse, Long projectId) {
//        String dummy = """
//                <message> I'm going to parse the response and save the files here </message>
//                <file path="src/App.jsx">
//                 file content here
//                </file>
//                """;
        Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);
        while (matcher.find()) {
            String filePath = matcher.group(1);
            String fileContent = matcher.group(2).trim();
            projectFileService.saveFile(projectId, filePath, fileContent);
        }


    }

    private void createChatSessionIfNotExists(Long projectId, Long userId) {

    }
}

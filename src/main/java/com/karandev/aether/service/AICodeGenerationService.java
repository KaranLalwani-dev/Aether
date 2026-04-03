package com.karandev.aether.service;

import com.karandev.aether.dto.chat.StreamResponse;
import reactor.core.publisher.Flux;

public interface AICodeGenerationService {
    Flux<StreamResponse> streamResponse(String message, Long projectId);
}

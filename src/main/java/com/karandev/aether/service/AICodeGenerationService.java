package com.karandev.aether.service;

import reactor.core.publisher.Flux;

public interface AICodeGenerationService {

    Flux<String> streamResponse(String message, Long projectId);
}

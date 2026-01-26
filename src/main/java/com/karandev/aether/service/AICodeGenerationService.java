package com.karandev.aether.service;

public interface AICodeGenerationService {

    Flux<String> streamResponse(String message, Long projectId);
}

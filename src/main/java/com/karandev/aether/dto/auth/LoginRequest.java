package com.karandev.aether.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}

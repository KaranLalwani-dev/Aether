package com.karandev.aether.dto.auth;

public record UserProfileResponse(
        Long id,
        String username,
        String name
) {
}

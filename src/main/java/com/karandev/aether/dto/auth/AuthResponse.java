package com.karandev.aether.dto.auth;

public record AuthResponse(
        String token,
        UserProfileResponse user
) {
}

package com.karandev.aether.dto.auth;

public record SignupRequest(
        String email,
        String name,
        String password
) {

}

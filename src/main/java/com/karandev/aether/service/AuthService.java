package com.karandev.aether.service;

import com.karandev.aether.dto.auth.AuthResponse;
import com.karandev.aether.dto.auth.LoginRequest;
import com.karandev.aether.dto.auth.SignupRequest;
import org.jspecify.annotations.Nullable;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}

package com.karandev.aether.service;

import com.karandev.aether.dto.auth.AuthResponse;
import com.karandev.aether.dto.auth.LoginRequest;
import com.karandev.aether.dto.auth.UserProfileResponse;
import org.jspecify.annotations.Nullable;

public interface UserService {

    UserProfileResponse getProfile(Long userId);
}

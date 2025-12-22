package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.auth.UserProfileResponse;
import com.karandev.aether.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserProfileResponse getProfile(Long userId) {
        return null;
    }
}

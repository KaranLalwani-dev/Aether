package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.auth.UserProfileResponse;
import com.karandev.aether.error.ResourceNotFoundException;
import com.karandev.aether.repository.UserRepository;
import com.karandev.aether.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    UserRepository userRepository;

    @Override
    public UserProfileResponse getProfile(Long userId) {
        return null;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", username));
    }
}

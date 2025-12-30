package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.auth.AuthResponse;
import com.karandev.aether.dto.auth.LoginRequest;
import com.karandev.aether.dto.auth.SignupRequest;
import com.karandev.aether.entity.User;
import com.karandev.aether.error.BadRequestException;
import com.karandev.aether.mapper.UserMapper;
import com.karandev.aether.repository.UserRepository;
import com.karandev.aether.security.AuthUtil;
import com.karandev.aether.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    UserMapper userMapper;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    AuthUtil authUtil;
    AuthenticationManager authenticationManager;

    @Override
    public AuthResponse signup(SignupRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(user -> {
            throw new BadRequestException("User already exists with username: " + request.username());
        });

        User user = userMapper.toUserEntityFromSignupRequest(request);
        user.setPassword(bCryptPasswordEncoder.encode(request.password()));
        user = userRepository.save(user);

        String token = authUtil.generateAccessToken(user);

        return new AuthResponse(token, userMapper.toUserProfileResponseFromUserEntity(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = (User) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(user);
        return new AuthResponse(token, userMapper.toUserProfileResponseFromUserEntity(user));
    }
}

package com.karandev.aether.controller;

import com.karandev.aether.dto.auth.AuthResponse;
import com.karandev.aether.dto.auth.LoginRequest;
import com.karandev.aether.dto.auth.SignupRequest;
import com.karandev.aether.dto.auth.UserProfileResponse;
import com.karandev.aether.service.AuthService;
import com.karandev.aether.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthController {

    AuthService authService;
    UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody @Valid SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile() {
        Long userId = 1L;
        return ResponseEntity.ok(userService.getProfile(userId));
    }

}

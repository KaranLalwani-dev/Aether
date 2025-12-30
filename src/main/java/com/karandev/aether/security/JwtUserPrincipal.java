package com.karandev.aether.security;


import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public record JwtUserPrincipal(
        Long userID,
        String username,
        List<GrantedAuthority> authorities
) {
}

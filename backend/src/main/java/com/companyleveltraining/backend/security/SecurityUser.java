package com.companyleveltraining.backend.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record SecurityUser(
    Long id,
    String accountNo,
    String role,
    String realName,
    String gender,
    String phone,
    String email,
    String status,
    String college,
    String major,
    String department,
    String positionTitle,
    String tokenId
) {
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }
}

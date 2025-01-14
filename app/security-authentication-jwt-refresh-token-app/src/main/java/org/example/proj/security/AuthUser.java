package org.example.proj.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-09
 */

@Getter
public class AuthUser extends User {

    private final UUID id;
    public AuthUser(String email, String password, Collection<? extends GrantedAuthority> authorities, UUID id) {
        super(email, password, authorities);
        this.id = id;
    }

    public String getEmail() {
        return getUsername();
    }
}

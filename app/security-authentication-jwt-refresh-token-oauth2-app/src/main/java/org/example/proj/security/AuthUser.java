package org.example.proj.security;

import lombok.Getter;
import org.example.proj.domain.user.AuthProvider;
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

    private String email;
    private String subject;
    public AuthUser(String email, String password, Collection<? extends GrantedAuthority> authorities, UUID id) {
        super(email, password, authorities);

        this.email = email;
        this.id = id;
    }

    public AuthUser(String sub, AuthProvider authProvider, Collection<? extends GrantedAuthority> authorities, UUID id){
        super(authProvider.name()+":"+sub,"EMPTY", authorities);
        this.id = id;
        this.subject = authProvider.name()+":"+sub;
    }

}

package org.example.proj.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.stereotype.Component;

/**
 * @author rival
 * @since 2024-12-10
 */

@Component
public class AuthDetailSource implements AuthenticationDetailsSource<HttpServletRequest, AuthDetails> {
    @Override
    public AuthDetails buildDetails(HttpServletRequest context) {
        return new AuthDetails(context);
    }
}

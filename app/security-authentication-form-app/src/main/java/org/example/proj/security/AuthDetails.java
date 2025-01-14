package org.example.proj.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.time.LocalDateTime;

/**
 * @author rival
 * @since 2024-12-10
 */


@Getter
public class AuthDetails extends WebAuthenticationDetails {

    private final LocalDateTime loginTime = LocalDateTime.now();

    public AuthDetails(HttpServletRequest request) {
        super(request);
    }


    @Override
    public String getRemoteAddress() {
        return super.getRemoteAddress();
    }

    @Override
    public String getSessionId() {
        return super.getSessionId();
    }


}

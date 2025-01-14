package org.example.proj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author rival
 * @since 2024-12-12
 */


@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ExpiredRefreshTokenException extends AuthenticationException {
    public ExpiredRefreshTokenException(String msg) {
        super(msg);
    }
}

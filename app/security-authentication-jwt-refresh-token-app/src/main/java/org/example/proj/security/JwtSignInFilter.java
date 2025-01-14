package org.example.proj.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.dto.req.SignInRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author rival
 * @since 2024-12-11
 */

@Slf4j
public class JwtSignInFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper;
    private final static String HTTP_METHOD = "POST";


    public JwtSignInFilter(String filterProcessesUrl, AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        super(new AntPathRequestMatcher(filterProcessesUrl, HTTP_METHOD), authenticationManager);
        this.objectMapper = objectMapper;
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {

        try (InputStream inputStream = request.getInputStream()) {
            SignInRequest signInRequest = objectMapper.readValue(inputStream, SignInRequest.class);

            String principal = signInRequest.getEmail();
            String credentials = signInRequest.getPassword();
            if (!StringUtils.hasText(principal) || !StringUtils.hasText(credentials)) {
                log.debug("Invalid Sign In request. (username={})",principal);
                throw new UsernameNotFoundException("Invalid Sign In request.");
            }
            // 로그인 요청 정보가 담긴 토큰만 만들기
            // Authentication(authenticated = false)
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(principal, credentials);

            // AuthenticationManager 토큰의 검증 위임
            // UsernamePasswordAuthenticationToken 타입을 검증할 수 있는
            // Authentication Provider 가 있으면 검증
            return getAuthenticationManager().authenticate(authRequest);
        }
    }



}

package org.example.proj.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.dto.req.SignInRequest;
import org.example.proj.dto.res.SignInFailureResponse;
import org.example.proj.dto.res.SignInSuccessResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
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
    private final JwtProvider jwtProvider;
    private final static String HTTP_METHOD = "POST";


    public JwtSignInFilter(String filterProcessesUrl, AuthenticationManager authenticationManager, ObjectMapper objectMapper, JwtProvider jwtProvider) {
        super(new AntPathRequestMatcher(filterProcessesUrl, HTTP_METHOD), authenticationManager);
        this.objectMapper = objectMapper;
        this.jwtProvider = jwtProvider;
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {

        if(!request.getMethod().equals(HTTP_METHOD)){
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        try (InputStream inputStream = request.getInputStream()) {
            SignInRequest signInRequest = objectMapper.readValue(inputStream, SignInRequest.class);

            String principal = signInRequest.getEmail();
            String credentials = signInRequest.getPassword();
            if (!StringUtils.hasText(principal) || !StringUtils.hasText(credentials)) {
                throw new UsernameNotFoundException(principal);
            }
            // Authentication(authenticated = false)
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(principal, credentials);
            return getAuthenticationManager().authenticate(authRequest);
        }
    }


    // success handler
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {

        AuthUser authUser = (AuthUser)authResult.getPrincipal();
        String token = jwtProvider.createToken(authUser.getId());


        log.info("New JWT is created for User(email={})", authUser.getEmail());

        SignInSuccessResponse body = SignInSuccessResponse.builder()
            .token(token)
            .userId(authUser.getId()).build();
        sendJsonResponse(response, HttpServletResponse.SC_OK, body);
    }


    // failure handler
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        SignInFailureResponse body = new SignInFailureResponse();
        sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, body);
    }


    private void sendJsonResponse(HttpServletResponse response, int statusCode, Object body) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(statusCode);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(body));
    }
}

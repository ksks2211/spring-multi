package org.example.proj.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.exception.InvalidRefreshTokenException;
import org.example.proj.service.RefreshTokenService;
import org.example.proj.util.CookieProvider;
import org.example.proj.util.ResponseProvider;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;


/**
 * @author rival
 * @since 2024-12-12
 */

@Slf4j
@RequiredArgsConstructor
public class JwtRefreshFilter extends OncePerRequestFilter {
    private final static String HTTP_METHOD = "POST";

    private final static String SIGN_IN_WITH_REFRESH_TOKEN_FAILURE_MESSAGE="Refresh Token validation failed!";



    private final String refreshSignInURI; // "/auth/renew"
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final CookieProvider cookieProvider;
    private final ResponseProvider responseProvider;


    private String extractRefreshToken(HttpServletRequest request){
        Cookie cookie = cookieProvider.extractRefreshCookie(request.getCookies())
            .orElseThrow(() -> new InvalidRefreshTokenException("Refresh Token is not found."));
        return cookie.getValue();
    }


    private boolean isRefreshRequest(HttpServletRequest request){
        return refreshSignInURI.equals(request.getRequestURI())&&request.getMethod().equalsIgnoreCase(HTTP_METHOD);
    }
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        boolean isRefreshRequest = isRefreshRequest(request);

        if(!isRefreshRequest){
            filterChain.doFilter(request, response);
        }else{
            handleRefreshToken(request, response);
        }
    }


    private void handleRefreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            String refreshToken = extractRefreshToken(request);
            AuthUser authUser = refreshTokenService.validateRefreshToken(UUID.fromString(refreshToken));
            String jsonWebToken = jwtProvider.createToken(authUser.getId());
            log.info("New JWT is created for User(email={})", authUser.getEmail());


            refreshTokenService.deleteRefreshTokenByUser(authUser.getId());
            String newRefreshToken = refreshTokenService.createRefreshToken(authUser.getId());
            Cookie cookie = cookieProvider.createRefreshCookie(newRefreshToken);
            response.addCookie(cookie);
            log.info("New Refresh Token is sent for User(email={})", authUser.getEmail());

            responseProvider.sendSignInSuccessResponse(response, authUser.getId(), jsonWebToken);
        } catch (Exception e) {
            log.info(SIGN_IN_WITH_REFRESH_TOKEN_FAILURE_MESSAGE, e);
            Cookie cookie = cookieProvider.createRefreshEraseCookie();
            response.addCookie(cookie);
            responseProvider.sendSignInFailureResponse(response, SIGN_IN_WITH_REFRESH_TOKEN_FAILURE_MESSAGE);
        }
    }
}

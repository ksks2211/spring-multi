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
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
//    private final JwtProvider jwtProvider;
    private final CookieProvider cookieProvider;
//    private final ResponseProvider responseProvider;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;


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

        if(isRefreshRequest){
            this.handleRefreshToken(request, response);
        }else{
            filterChain.doFilter(request, response);
        }


    }


    private void handleRefreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try{

            // Extract Refresh Token
            String refreshToken = extractRefreshToken(request);

            // Find a User who owns the Refresh Token
            AuthUser authUser = refreshTokenService.validateRefreshToken(UUID.fromString(refreshToken));

            // Delete the Used Refresh Token
            refreshTokenService.deleteRefreshTokenByUser(authUser.getId());
            log.info("Old Refresh Token is deleted for User(uuid={})",authUser.getId());

            // Create new Refresh Token for later usage
            String newRefreshToken = refreshTokenService.createRefreshToken(authUser.getId());
            Cookie cookie = cookieProvider.createRefreshCookie(newRefreshToken);
            response.addCookie(cookie);
            log.info("New Refresh Token is created for User(email={})", authUser.getEmail());



            // Create Authentication
            var authResult = new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
            authResult.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );

            authenticationSuccessHandler.onAuthenticationSuccess(request, response, authResult);
        } catch (Exception e) {
            log.info(SIGN_IN_WITH_REFRESH_TOKEN_FAILURE_MESSAGE, e);
            var ex = new AuthenticationServiceException(SIGN_IN_WITH_REFRESH_TOKEN_FAILURE_MESSAGE);
            authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }
    }
}

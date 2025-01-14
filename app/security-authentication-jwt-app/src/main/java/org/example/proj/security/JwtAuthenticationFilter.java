package org.example.proj.security;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.service.AppUserService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-11
 */


@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtProvider jwtProvider;
    private final AppUserService appUserService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            JwtVerifyResult jwtVerifyResult = jwtProvider.verifyToken(token);
            if(jwtVerifyResult.isVerified()){

                UUID userId = jwtVerifyResult.getUserId();


                // if UsernameNotFoundException  =>  AuthenticationEntryPoint
                AuthUser user = (AuthUser) appUserService.loadUserByUUID(userId);


                // Create authentication
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
                );
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );


                // Add authentication into Security Context
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

                log.info("User(email={}) is authenticated.",user.getEmail());
            }else if(jwtVerifyResult.isDecoded()){
                log.info("User(uuid={}) fail to authenticate.",jwtVerifyResult.getSubject());
            }else{
                log.info("Invalid authentication attempt");
            }
        }
        filterChain.doFilter(request,response);
    }
}

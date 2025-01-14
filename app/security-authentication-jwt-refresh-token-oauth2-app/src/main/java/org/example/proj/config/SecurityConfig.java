package org.example.proj.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.domain.user.AuthProvider;
import org.example.proj.dto.res.ForbiddenAccessResponse;
import org.example.proj.dto.res.UnauthorizedAccessResponse;
import org.example.proj.security.*;
import org.example.proj.service.AppUserService;
import org.example.proj.service.NoOpOAuth2AuthorizedClientService;
import org.example.proj.service.RefreshTokenService;
import org.example.proj.util.CookieProvider;
import org.example.proj.util.ResponseProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

import static org.example.proj.util.FilterUtils.reorderFilters;
import static org.example.proj.util.SessionUtils.invalidateSession;


/**
 * @author rival
 * @since 2024-12-09
 */


@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    // login/refresh
    public static final String REFRESH_SIGN_IN_URL = "/auth/renew";


    // login/local
    public static final String SIGN_IN_URL = "/auth/sign-in";


    public static final String LOGOUT_URL = "/auth/logout";


    // /oauth2/authorization/{registrationId}
    public static final String OAUTH2_LOGIN_URL = "/oauth2/authorization";


    private final JwtProvider jwtProvider;
    private final AuthenticationConfiguration authConfig;
    private final AppUserService appUserService;
    private final ObjectMapper objectMapper;
    private final ResponseProvider responseProvider;
    private final CookieProvider cookieProvider;
    private final RefreshTokenService refreshTokenService;



    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        final var config = new CorsConfiguration();


        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "DELETE", "PUT"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setExposedHeaders(Collections.singletonList("*"));


        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(true);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {




        // CORS
        http.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()));

        // CSRF = disabled
        http.csrf(AbstractHttpConfigurer::disable);



        http.authorizeHttpRequests(authRequests -> authRequests
            .requestMatchers(new AntPathRequestMatcher("/auth/**"), new AntPathRequestMatcher("/error")).permitAll()
            .requestMatchers(new AntPathRequestMatcher(LOGOUT_URL)).authenticated()
            .anyRequest().authenticated()
        );

        // Session Policy = Stateless
        http.sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::none)
        );


        // JWT
        http.addFilterBefore(jwtRefreshFilter(), BasicAuthenticationFilter.class);
        http.addFilterAt(jwtAuthenticationFilter(), BasicAuthenticationFilter.class);
        http.addFilterAt(jwtSignInFilter(), UsernamePasswordAuthenticationFilter.class);


        // Exception Handler
        http.exceptionHandling(config -> config
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler(accessDeniedHandler()));


        // http.logout(AbstractHttpConfigurer::disable);
        http.logout(config -> config
            .addLogoutHandler(logoutHandler())
            .deleteCookies(cookieProvider.getRefreshCookieName())
            .invalidateHttpSession(true) // in case session exists
            .logoutUrl(LOGOUT_URL)
            .logoutSuccessHandler(logoutSuccessHandler())
        );


        // oauth2
        http.oauth2Login(config -> config
            .authorizationEndpoint(endpoint -> endpoint.baseUri(OAUTH2_LOGIN_URL))
            .successHandler(authenticationSuccessHandler())
            .failureHandler(authenticationFailureHandler())
            .authorizedClientService(new NoOpOAuth2AuthorizedClientService())
        );


        var chain = http.build();
        reorderFilters(chain.getFilters());

        return chain;
    }


    private JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, appUserService, authenticationEntryPoint());
    }


    // Logout Handlers

    private LogoutHandler logoutHandler() {
        return ((request, response, authentication) -> {
            if (authentication != null && authentication.getPrincipal() instanceof AuthUser authUser) {
                refreshTokenService.deleteRefreshTokenByUser(authUser.getId());
                log.info("Delete Refresh token for logout user(uuid={})", authUser.getId());
            }
        });
    }


    private LogoutSuccessHandler logoutSuccessHandler() {
        return ((request, response, authentication) -> {

            // 로그인 하지 않은 유저의 로그아웃 요청에 대한 응답
            // 400 : bad request - 잘못된 요청으로 취급
            // 401 : unauthorized - 허가되지 않은 요청으로 취급
            // 204 : no content - 로그인 상태가 아니므로 성공한것과 마찬가지로 취급
            if (authentication == null) {
                log.info("Logout attempt of non-logged-in user");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                log.info("Logout succeeded");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }

        });
    }


    // Jwt & Refresh Token Filters & Handlers (login)

    private JwtRefreshFilter jwtRefreshFilter() {
        return new JwtRefreshFilter(REFRESH_SIGN_IN_URL, refreshTokenService, cookieProvider, authenticationSuccessHandler(), authenticationFailureHandler());
    }

    private JwtSignInFilter jwtSignInFilter() throws Exception {
        JwtSignInFilter jwtSignInFilter = new JwtSignInFilter(SIGN_IN_URL, authenticationManager(), objectMapper);
        jwtSignInFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        jwtSignInFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return jwtSignInFilter;
    }


    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {

        // publish JWT
        return (request, response, authResult) -> {

            Object principal = authResult.getPrincipal();
            AuthUser authUser = null;


            if (principal instanceof AuthUser) {
                authUser = (AuthUser) principal;
            } else if (principal instanceof CustomOidcUser oidcUser) {

                String registrationId = oidcUser.getRegistrationId();
                log.info("RegistrationId : {}", registrationId);
                // map  registrationId => provider
                AuthProvider provider = AuthProvider.GOOGLE;

                String sub = oidcUser.getSubject();
                String name = oidcUser.getName();
                String email = oidcUser.getEmail();
                authUser = appUserService.createOrUpdateOAuth2AppUser(provider, sub, name, email);


            }

            if (authUser != null) {
                String token = jwtProvider.createToken(authUser.getId());
                log.info("New JWT is created for User(uuid={})", authUser.getId());

                // Clear session for oauth2
                invalidateSession(request.getSession(false));
                // response
                responseProvider.sendSignInSuccessResponse(response, authUser.getId(), token);
            } else {
                throw new AuthenticationServiceException("Authenticated but cannot publish JWT!");
            }
        };
    }


    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            log.info("JWT Sign In failed", exception);
            invalidateSession(request.getSession(false));
            responseProvider.sendSignInFailureResponse(response);
        };
    }


    // Failed authentication attempt or Access without authentication
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            UnauthorizedAccessResponse body = new UnauthorizedAccessResponse();
            log.info("Unauthorized Exception : {}", authException.getMessage());
            responseProvider.sendJsonBody(response, HttpServletResponse.SC_UNAUTHORIZED, body);
        };
    }


    // Access with authentication but without proper authorization
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ForbiddenAccessResponse body = new ForbiddenAccessResponse();
            log.info("Access Denied Exception : {}", accessDeniedException.getMessage());
            responseProvider.sendJsonBody(response, HttpServletResponse.SC_FORBIDDEN, body);
        };
    }

}

package org.example.proj.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.domain.user.AppUserRepository;
import org.example.proj.dto.res.ForbiddenAccessResponse;
import org.example.proj.dto.res.UnauthorizedAccessResponse;
import org.example.proj.security.*;
import org.example.proj.service.AppUserService;
import org.example.proj.service.RefreshTokenService;
import org.example.proj.util.CookieProvider;
import org.example.proj.util.ResponseProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;


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


    public static final String REFRESH_SIGN_IN_URL = "/auth/renew";
    public static final String SIGN_IN_URL = "/auth/sign-in";
    public static final String LOGOUT_URL = "/auth/logout";




    private final JwtProvider jwtProvider;
    private final AuthenticationConfiguration authConfig;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;
    private final ResponseProvider responseProvider;
    private final CookieProvider cookieProvider;
    private final RefreshTokenService refreshTokenService;




    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

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
    public AppUserService appUserService() {
        return new AppUserService(appUserRepository, passwordEncoder());
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(true);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CORS
        http.cors(
            corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource())
        );

        // CSRF = disabled
        http.csrf(AbstractHttpConfigurer::disable);


        http.authorizeHttpRequests(authRequests ->
            authRequests
                .requestMatchers(
                    new AntPathRequestMatcher("/auth/**"),
                    new AntPathRequestMatcher("/error")
                ).permitAll().anyRequest().authenticated()
        );

        // Session Policy = Stateless
        http.sessionManagement(httpSecuritySessionManagementConfigurer ->
            httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );


        // JWT
//        http.httpBasic(withDefaults());
        http.addFilterBefore(jwtRefreshFilter(), BasicAuthenticationFilter.class);
        http.addFilterAt(jwtAuthenticationFilter(), BasicAuthenticationFilter.class);
        http.addFilterAt(jwtSignInFilter(), UsernamePasswordAuthenticationFilter.class);



        // Exception Handler
        http.exceptionHandling(this::exceptionHandlingConfig);


//        http.logout(AbstractHttpConfigurer::disable);
        http.logout(config ->
            config.addLogoutHandler(logoutHandler())
                .deleteCookies(cookieProvider.getRefreshCookieName())
                .logoutUrl(LOGOUT_URL)
                .logoutSuccessHandler(logoutSuccessHandler())
        );



        var chain = http.build();
        reorderFilters(chain.getFilters());


        return chain;
    }

    private JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, appUserService(), authenticationEntryPoint());
    }


    // Move LogoutFilter behind JwtAuthenticationFilter
    private void reorderFilters(List<Filter> filters){
        LogoutFilter logoutFilter = null;

        for(int i=0;i< filters.size();i++){
            if(filters.get(i) instanceof LogoutFilter){
                logoutFilter = (LogoutFilter) filters.remove(i);
                break;
            }
        }

        if (logoutFilter != null) {
            for (int i = 0; i < filters.size(); i++) {
                if (filters.get(i) instanceof JwtAuthenticationFilter) {
                    filters.add(i + 1, logoutFilter);
                    break;
                }
            }
        }
    }

    private LogoutHandler logoutHandler() {
        return ((request, response, authentication) -> {
            if (authentication != null && authentication.getPrincipal() instanceof AuthUser authUser) {
                refreshTokenService.deleteRefreshTokenByUser(authUser.getId());
                log.info("Delete Refresh token for logout user(uuid={})",authUser.getId());
            }
        });
    }


    private LogoutSuccessHandler logoutSuccessHandler(){
        return ((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT));
    }

    private JwtRefreshFilter jwtRefreshFilter() {
        return new JwtRefreshFilter(REFRESH_SIGN_IN_URL, refreshTokenService, jwtProvider, cookieProvider, responseProvider);
    }

    private JwtSignInFilter jwtSignInFilter() throws Exception {
        JwtSignInFilter jwtSignInFilter = new JwtSignInFilter(SIGN_IN_URL, authenticationManager(), objectMapper);
        jwtSignInFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        jwtSignInFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return jwtSignInFilter;
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authResult) -> {
            AuthUser authUser = (AuthUser) authResult.getPrincipal();
            String token = jwtProvider.createToken(authUser.getId());
            log.info("New JWT is created for User(email={})", authUser.getEmail());
            responseProvider.sendSignInSuccessResponse(response, authUser.getId(), token);
        };
    }

    private AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            log.info("JWT Sign In failed", exception);
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
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ForbiddenAccessResponse body = new ForbiddenAccessResponse();
            log.info("Access Denied Exception : {}", accessDeniedException.getMessage());
            responseProvider.sendJsonBody(response, HttpServletResponse.SC_FORBIDDEN, body);
        };
    }


    private void exceptionHandlingConfig(ExceptionHandlingConfigurer<HttpSecurity> exceptionHandling) {
        exceptionHandling
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler(accessDeniedHandler());
    }
}

package org.example.proj.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.domain.user.AppUserRepository;
import org.example.proj.dto.res.ForbiddenAccessResponse;
import org.example.proj.dto.res.UnauthorizedAccessResponse;
import org.example.proj.security.JwtAuthenticationFilter;
import org.example.proj.security.JwtProvider;
import org.example.proj.security.JwtSignInFilter;
import org.example.proj.service.AppUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
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



    private final static String SIGN_IN_URL = "/auth/sign-in";

    private final JwtProvider jwtProvider;
    private final AuthenticationConfiguration authConfig;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;

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
    public AppUserService appUserService(){
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
        http.addFilterAt(new JwtAuthenticationFilter(jwtProvider, appUserService()), BasicAuthenticationFilter.class);
        http.addFilterAt(new JwtSignInFilter(SIGN_IN_URL,authenticationManager(), objectMapper, jwtProvider), UsernamePasswordAuthenticationFilter.class);


        // Exception Handler
        http.exceptionHandling(this::exceptionHandlingConfig);


        return http.build();
    }




    // Failed authentication attempt or Access without authentication
    private AuthenticationEntryPoint authenticationEntryPoint(){
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            UnauthorizedAccessResponse body = new UnauthorizedAccessResponse();
            log.info("Unauthorized Exception : {}", authException.getMessage());
            writeResponseJsonBody(response, body);
        };
    }


    // Access with authentication but without proper authorization
    private AccessDeniedHandler accessDeniedHandler(){
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            ForbiddenAccessResponse body = new ForbiddenAccessResponse();
            log.info("Access Denied Exception : {}", accessDeniedException.getMessage());
            writeResponseJsonBody(response, body);
        };
    }

    private void writeResponseJsonBody(HttpServletResponse response, Object body) throws IOException {
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    private void exceptionHandlingConfig(ExceptionHandlingConfigurer<HttpSecurity> exceptionHandling) {
         exceptionHandling
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler(accessDeniedHandler());
    }
}

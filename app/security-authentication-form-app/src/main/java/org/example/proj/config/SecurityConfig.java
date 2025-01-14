package org.example.proj.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.domain.user.AppUserRepository;
import org.example.proj.security.AuthDetailSource;
import org.example.proj.security.AuthDetails;
import org.example.proj.service.AppUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
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
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
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


    public final static String LOGIN_URI = "/auth/sign-in";
    public final static String LOGOUT_URI = "/auth/logout";


    private final AuthDetailSource authDetailSource;
    private final AppUserRepository appUserRepository;
    private final DataSource dataSource;


    @Value("${security.remember-me.key}")
    private String rememberMeKey;

    @Value("${security.remember-me.duration}")
    private int rememberMeDuration;


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
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CORS
        http.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()));

        // CSRF : disabled
        http.csrf(AbstractHttpConfigurer::disable);


        http.authorizeHttpRequests(authRequests -> authRequests.requestMatchers(new AntPathRequestMatcher("/auth/**"), new AntPathRequestMatcher("/error")).permitAll().anyRequest().authenticated());

        // Session Policy
        http.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        // Form Login
        http.formLogin(configurer -> configurer.usernameParameter("email").passwordParameter("password").authenticationDetailsSource(authDetailSource)
            // .loginPage("/auth/sign-in")
            .loginProcessingUrl(LOGIN_URI).permitAll().successHandler(authenticationSuccessHandler()).failureHandler(authenticationFailureHandler()));

        // log out
        http.logout(configurer -> configurer.logoutUrl(LOGOUT_URI).logoutSuccessHandler(logoutSuccessHandler()));


        http.rememberMe(configurer -> configurer.key(rememberMeKey).rememberMeParameter("remember-me").tokenValiditySeconds(rememberMeDuration)

            .authenticationSuccessHandler(rememberMeAuthenticationSuccessHandler()).tokenRepository(persistentTokenRepository()));


        http.exceptionHandling(this::exceptionHandlingConfig);


        return http.build();
    }


    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
//            UnauthorizedAccessResponse body = new UnauthorizedAccessResponse();
            log.info("Unauthorized Exception : {}", authException.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.info("Access Denied Exception : {}", accessDeniedException.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        };
    }


    private void exceptionHandlingConfig(ExceptionHandlingConfigurer<HttpSecurity> exceptionHandling) {
        exceptionHandling.authenticationEntryPoint(authenticationEntryPoint()).accessDeniedHandler(accessDeniedHandler());
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return ((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_OK));
    }


    private AuthenticationSuccessHandler rememberMeAuthenticationSuccessHandler() {
        return ((request, response, authentication) -> {

            log.info("Remember me authentication.");
            log.info("Authentication : {}", authentication);
            if (authentication instanceof RememberMeAuthenticationToken token) {
                log.info("set details");
                AuthDetails details = authDetailSource.buildDetails(request);
                token.setDetails(details);
                request.getRequestDispatcher(request.getRequestURI()).forward(request, response);
            }
        });
    }


    private AuthenticationFailureHandler authenticationFailureHandler() {
        return ((request, response, exception) -> response.setStatus(HttpServletResponse.SC_BAD_REQUEST));
    }

    private LogoutSuccessHandler logoutSuccessHandler() {
        return ((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_OK));
    }
}

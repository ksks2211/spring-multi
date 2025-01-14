package org.example.proj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @author rival
 * @since 2024-12-09
 */


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


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

        // CSRF : disabled
        http.csrf(AbstractHttpConfigurer::disable);


        http.authorizeHttpRequests(authRequests ->
            authRequests
                .requestMatchers(
                    new AntPathRequestMatcher("/auth/**")
                ).permitAll().anyRequest().authenticated()
        );

        // Session Policy
        http.sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        // Basic Login
        http.httpBasic(withDefaults());




        http.logout(configurer->configurer.logoutUrl("/auth/log-out"));


        return http.build();
    }

}

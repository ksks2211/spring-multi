package org.example.proj.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.dto.req.SignUpRequest;
import org.example.proj.security.AuthUser;
import org.example.proj.service.AppUserService;
import org.example.proj.service.RefreshTokenService;
import org.example.proj.util.CookieProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * @author rival
 * @since 2024-12-09
 */

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AppUserService appUserService;
    private final RefreshTokenService refreshTokenService;
    private final CookieProvider cookieProvider;




    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest request){
        appUserService.createAppUser(request.getEmail(),request.getPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/auth/sign-in"));
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).build();
    }




    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Object user){
        return ResponseEntity.ok(user);
    }




    @PreAuthorize("isAuthenticated()")
    @PostMapping("/refresh")
    public void getRefreshToken(@AuthenticationPrincipal AuthUser user, HttpServletResponse response){
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());
        Cookie cookie = cookieProvider.createRefreshCookie(refreshToken);
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        log.info("Refresh Token({}) is issued for User(email={}) : ",refreshToken, user.getEmail());
    }


}

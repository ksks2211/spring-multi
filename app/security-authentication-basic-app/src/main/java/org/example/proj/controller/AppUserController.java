package org.example.proj.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.proj.dto.req.SignUpRequest;
import org.example.proj.service.AppUserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * @author rival
 * @since 2024-12-09
 */

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping("")
    public String test(){
        return "Hello World";
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest request){
        appUserService.createAppUser(request.getEmail(),request.getPassword());


        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/auth/sign-in"));

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).build();
    }




    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(user);
    }

}

package org.example.proj.service;

import lombok.RequiredArgsConstructor;
import org.example.proj.domain.user.AppUser;
import org.example.proj.domain.user.AppUserRepository;
import org.example.proj.exception.EmailAlreadyExistsException;
import org.example.proj.security.AuthUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author rival
 * @since 2024-12-09
 */

@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;




    @Transactional
    public void createAppUser(String email, String password){
        if(appUserRepository.existsByEmail(email)){
            throw new EmailAlreadyExistsException(String.format("Email(%s) already exists.", email));
        }
        AppUser appUser = AppUser.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .gender(AppUser.Gender.MALE)
            .build();
        appUserRepository.save(appUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(String.format("User(%s) not found.", email)));
        return fromEntity(appUser);
    }



    public static AuthUser fromEntity(AppUser appUser){
        return new AuthUser(
            appUser.getEmail(),
            appUser.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            appUser.getId()
        );
    }



}

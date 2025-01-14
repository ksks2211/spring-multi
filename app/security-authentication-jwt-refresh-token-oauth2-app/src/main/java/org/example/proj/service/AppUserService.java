package org.example.proj.service;

import lombok.RequiredArgsConstructor;
import org.example.proj.domain.user.AppUser;
import org.example.proj.domain.user.AppUserRepository;
import org.example.proj.domain.user.AuthProvider;
import org.example.proj.exception.EmailAlreadyExistsException;
import org.example.proj.security.AuthUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-09
 */

@Service
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
        AppUser appUser = appUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(String.format("User(email=%s) not found.", email)));
        return fromEntity(appUser);
    }



    public UserDetails loadUserByUUID(UUID uuid) throws UsernameNotFoundException{
        AppUser appUser = appUserRepository.findById(uuid).orElseThrow(() -> new UsernameNotFoundException(String.format("User(uuid=%s) not found.", uuid)));
        AuthUser authUser = appUser.isSocial() ? fromOAuth2Entity(appUser) : fromEntity(appUser);
        authUser.eraseCredentials();
        return authUser;
    }



    @Transactional
    public AuthUser createOrUpdateOAuth2AppUser(AuthProvider provider, String sub, String username, String email){
        AppUser appUser = appUserRepository.findByProviderAndProvidedId(provider, sub).orElseGet(() -> toEntity(provider, sub));

        // Update
        appUser.setProvidedEmail(email);
        appUser.setProvidedName(username);

        appUserRepository.save(appUser);
        return fromOAuth2Entity(appUser);
    }


    private AppUser toEntity(AuthProvider provider, String providedId){
          return AppUser.builder().provider(provider).providedId(providedId).isSocial(true).build();
    }



    public static AuthUser fromEntity(AppUser appUser){
        return new AuthUser(
            appUser.getEmail(),
            appUser.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            appUser.getId()
        );
    }


    public static AuthUser fromOAuth2Entity(AppUser appUser){
        return new AuthUser(
            appUser.getProvidedId(),
            appUser.getProvider(),
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            appUser.getId()
        );
    }






}

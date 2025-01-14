package org.example.proj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.domain.user.AppUser;
import org.example.proj.domain.user.AppUserRepository;
import org.example.proj.exception.ExpiredRefreshTokenException;
import org.example.proj.exception.InvalidRefreshTokenException;
import org.example.proj.security.AuthUser;
import org.example.proj.security.RefreshToken;
import org.example.proj.security.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.example.proj.service.AppUserService.fromEntity;

/**
 * @author rival
 * @since 2024-12-12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;

    @Value("${auth.refresh-token.exp-hours}")
    private int REFRESH_EXP_HOURS;


    private LocalDateTime getExpiresAt(){
        return LocalDateTime.now().plusHours(REFRESH_EXP_HOURS);
    }






    @Transactional
    public String createRefreshToken(UUID userId){
        if(!appUserRepository.existsById(userId)){
            throw new UsernameNotFoundException("User(uuid="+userId+") Not Found.");
        }

        AppUser user = AppUser.builder().id(userId).build();

        RefreshToken refreshToken;
        Optional<RefreshToken> optional = refreshTokenRepository.findByUser(user);


        if(optional.isPresent() && !optional.get().isExpired()){

            refreshToken = optional.get();
        }else{
            optional.ifPresent(tk->{
                refreshTokenRepository.deleteById(tk.getId());
                refreshTokenRepository.flush();
            });
            refreshToken = RefreshToken.builder()
                .expiresAt(getExpiresAt())
                .user(user)
                .build();
            refreshTokenRepository.save(refreshToken);
        }
        return refreshToken.getId().toString();
    }



    @Transactional
    public AuthUser validateRefreshToken(UUID tokenId){ // check if token is UUID
        RefreshToken refreshToken = refreshTokenRepository.findById(tokenId).orElseThrow(() -> new InvalidRefreshTokenException("Invalid Refresh Token"));
        if(refreshToken.isExpired()){
            refreshTokenRepository.delete(refreshToken);
            throw new ExpiredRefreshTokenException("Token Expired");
        }
        AppUser user = refreshToken.getUser();
        return fromEntity(user);
    }


    @Transactional
    public void deleteRefreshTokenByUser(UUID userId){
        refreshTokenRepository.deleteByUser(AppUser.builder().id(userId).build());
    }

    @Transactional
    public void deleteRefreshTokenByUserAndTokenId(UUID tokenId, UUID userId){
        refreshTokenRepository.deleteByIdAndUser(tokenId, AppUser.builder().id(userId).build());
    }

}

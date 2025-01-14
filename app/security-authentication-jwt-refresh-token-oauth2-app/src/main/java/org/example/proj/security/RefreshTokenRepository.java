package org.example.proj.security;

import org.example.proj.domain.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-11
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByUser(AppUser user);

    Optional<RefreshToken> findByIdAndUser(UUID id, AppUser user);



//    @Query("delete from RefreshToken rt  where rt.expiresAt < CURRENT_TIMESTAMP ")
    @Modifying
    @Query("delete from RefreshToken rt  where rt.expiresAt < :currentTime and rt.user = :user")
    int deleteExpiredTokenByUser(@Param("currentTime") LocalDateTime currentTime, @Param("user") AppUser user);




    @Modifying
    @Query("delete from RefreshToken rt where rt.user = :user")
    void deleteByUser(@Param("user") AppUser user);



    void deleteByIdAndUser(UUID id, AppUser user);



    Long countByUser(AppUser user);

}

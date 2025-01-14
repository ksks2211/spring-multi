package org.example.proj.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-02
 */
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {


    boolean existsByEmail(String email);





    Optional<AppUser> findByEmail(String email);




    // non-local user only
    Optional<AppUser> findByProviderAndProvidedId(AuthProvider provider, String providedId);
}

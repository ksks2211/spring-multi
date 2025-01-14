package org.example.proj.security;

import jakarta.persistence.*;
import lombok.*;
import org.example.proj.domain.user.AppUser;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-11
 */




@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime expiresAt;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @ToString.Exclude
    private AppUser user;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

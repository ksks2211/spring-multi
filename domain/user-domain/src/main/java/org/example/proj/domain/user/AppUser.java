package org.example.proj.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.example.proj.domain.base.BaseEntity;

import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-02
 */

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class AppUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String email;

    private String password;



    @Enumerated(EnumType.STRING)
    private Gender gender;




    public enum Gender{
        MALE, FEMALE
    }


}

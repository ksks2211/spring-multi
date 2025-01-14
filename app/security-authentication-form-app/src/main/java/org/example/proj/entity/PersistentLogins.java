package org.example.proj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @author rival
 * @since 2024-12-13
 */


@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PersistentLogins {

    @Id
    private String series;



    private String username;

    private String token;


    @Column(columnDefinition = "datetime")
    private Instant lastUsed;

}

package org.example.proj.domain.post;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

/**
 * @author rival
 * @since 2024-11-25
 */

//@SQLDelete(sql="UPDATE post SET deleted=true, deleted_at=now()  WHERE id =?")

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    private String content;


    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;


    @Column
    private LocalDateTime deletedAt;
}

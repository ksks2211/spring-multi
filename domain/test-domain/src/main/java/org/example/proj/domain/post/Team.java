package org.example.proj.domain.post;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rival
 * @since 2024-11-28
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;



    // read-only
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @BatchSize(size=5)
    private List<Member> members = new ArrayList<>();

}

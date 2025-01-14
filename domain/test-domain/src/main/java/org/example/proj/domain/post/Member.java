package org.example.proj.domain.post;

import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
public class Member {

    @Id
    @GeneratedValue
    private Long id;

    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id")
    @ToString.Exclude
    private Team team;




    @Embedded
    private Address address;



    @Transient
    @Builder.Default
    private Set<String> roles = new HashSet<>();



    @Access(AccessType.PROPERTY)
    private String getRolesString(){
        return String.join(",", roles);
    }


    public void setRolesString(String rolesString){
        if(rolesString == null){
            roles = new HashSet<>();
            return;
        }
        String[] arr = rolesString.split(",");
        roles = new HashSet<>(Arrays.asList(arr));
    }


}

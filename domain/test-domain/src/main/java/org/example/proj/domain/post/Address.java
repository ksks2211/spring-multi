package org.example.proj.domain.post;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author rival
 * @since 2024-12-09
 */

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Address {


    private String city;
    private String district;
    private String zipCode;

}

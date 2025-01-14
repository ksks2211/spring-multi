package org.example.proj;

import lombok.*;

import java.io.Serializable;

/**
 * @author rival
 * @since 2025-01-15
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PostDto implements Serializable {


    private Long id;
    private String title;
    private String content;
}

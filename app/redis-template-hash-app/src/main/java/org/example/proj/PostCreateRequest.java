package org.example.proj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rival
 * @since 2025-01-13
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    private String title;
    private String content;

}

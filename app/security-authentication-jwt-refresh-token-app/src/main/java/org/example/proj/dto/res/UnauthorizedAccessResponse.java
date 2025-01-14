package org.example.proj.dto.res;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author rival
 * @since 2024-12-11
 */
@NoArgsConstructor
@Data
public class UnauthorizedAccessResponse implements Serializable {
    private final String message = "Unauthorized Request!";
}

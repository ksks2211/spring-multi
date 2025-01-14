package org.example.proj.dto.res;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rival
 * @since 2024-12-11
 */


@NoArgsConstructor
@Data
public class SignInFailureResponse {
    private String message = "Check your email and password.";
}

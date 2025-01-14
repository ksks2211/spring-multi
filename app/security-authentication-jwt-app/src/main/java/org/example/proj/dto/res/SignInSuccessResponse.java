package org.example.proj.dto.res;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-11
 */

@Data
@Builder
public class SignInSuccessResponse {

    @Builder.Default
    private String message = "Successfully logged in.";
    private String token;
    private UUID userId;
}

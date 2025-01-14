package org.example.proj.security;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-10
 */

@Data
@Builder
public class JwtVerifyResult {

    private boolean verified;
    private boolean decoded;
    private String subject;
    private UUID userId;
    private Date expiresAt;
}

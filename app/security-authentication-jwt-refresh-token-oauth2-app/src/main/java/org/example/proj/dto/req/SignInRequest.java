package org.example.proj.dto.req;

import lombok.Data;

/**
 * @author rival
 * @since 2024-12-11
 */
@Data
public class SignInRequest {

    private String email;
    private String password;
}

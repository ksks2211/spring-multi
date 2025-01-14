package org.example.proj.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author rival
 * @since 2024-12-09
 */


@Data
public class SignUpRequest {

    @Email
    @NotBlank
    private String email;



    @Size(min = 6, max = 50)
    @NotBlank
    private String password;
}

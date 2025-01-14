package org.example.proj.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author rival
 * @since 2024-12-18
 */

@RequiredArgsConstructor
@Getter
public enum AuthProvider {

    LOCAL("local"), GOOGLE("google");

    private final String registrationId;

}

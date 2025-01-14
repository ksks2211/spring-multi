package org.example.proj.util;

import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-12
 */
public class UUIDUtils {

    public static boolean isValidUUID(String uuidString) {
        try {
            UUID uuid = UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

}

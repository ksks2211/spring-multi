package org.example.proj.util.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rival
 * @since 2025-01-20
 */
class CustomStringUtilsTest {


    @Test
    public void test(){
        String sample = "  ";
        assertFalse(CustomStringUtils.hasText(sample));
    }

}
package org.example.proj.util.base;



/**
 * @author rival
 * @since 2025-01-20
 */
public class CustomStringUtils {
    public static boolean hasText(String input){
        return org.springframework.util.StringUtils.hasText(input);
    }




    public static String abbreviate(String input, int maxWidth){
        return org.apache.commons.lang3.StringUtils.abbreviate(input, maxWidth);
    }

    public static String reverse(String input){
        return org.apache.commons.lang3.StringUtils.reverse(input);
    }

    private CustomStringUtils(){}
}

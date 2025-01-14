package org.example.proj.util;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

import static org.example.proj.config.SecurityConfig.REFRESH_SIGN_IN_URL;

/**
 * @author rival
 * @since 2024-12-12
 */
@Component
public class CookieProvider {

    @Value("${auth.refresh-token.exp-hours}")
    private int REFRESH_EXP_HOURS;

    @Value("${auth.refresh-token.name}")
    private String REFRESH_COOKIE_NAME;



    public String getRefreshCookieName(){
        return this.REFRESH_COOKIE_NAME;
    }

    public Cookie createRefreshCookie(String refreshToken){
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME,refreshToken);

        cookie.setHttpOnly(true);
        cookie.setPath(REFRESH_SIGN_IN_URL);
        cookie.setMaxAge(60*60*REFRESH_EXP_HOURS);
        return cookie;
    }

    public Cookie createRefreshEraseCookie(){
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME,null);
        cookie.setHttpOnly(true);
        cookie.setPath(REFRESH_SIGN_IN_URL);
        cookie.setMaxAge(0);

        return cookie;
    }


    public Optional<Cookie> extractRefreshCookie(Cookie[] cookies){


        if(cookies == null || cookies.length==0){
            return Optional.empty();
        }

        return Arrays.stream(cookies).filter(cookie->cookie.getName().equals(REFRESH_COOKIE_NAME)).findFirst();
    }

}

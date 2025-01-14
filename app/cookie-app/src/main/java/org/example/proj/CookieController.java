package org.example.proj;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @author rival
 * @since 2024-12-10
 */


@RestController
@RequestMapping("cookie")
public class CookieController {


    private final String cookieName = "test-cookie";
    private static final String SECRET_KEY = "MySuperSecretKey!"; // 16자

    // 암호화 메서드
    private String encrypt(String data) throws Exception {
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 복호화 메서드
    private String decrypt(String encryptedData) throws Exception {
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }


    @GetMapping("/send-cookie")
    public ResponseEntity<String> sendCookie(HttpServletResponse res){
        String cookieVale = "testValue";
        Cookie cookie = new Cookie(cookieName, cookieVale);
        cookie.setPath("/cookie/read-cookie");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(7 * 24 * 60 * 60); // 1 week
        res.addCookie(cookie);
        return ResponseEntity.ok("Cookie sent successfully");
    }



    @GetMapping("/send-encrypted-cookie")
    public String sendEncryptedCookie(HttpServletResponse response) throws Exception {
        String originalValue = "sensitiveData";
        String encryptedValue = encrypt(originalValue);

        Cookie cookie = new Cookie("secureCookie", encryptedValue);
        cookie.setHttpOnly(true);
//        cookie.setSecure(true); // HTTPS에서만 동작
        cookie.setPath("/");
        cookie.setMaxAge(3600); // 1시간
        response.addCookie(cookie);

        return "Encrypted cookie sent!";
    }

    @GetMapping("/read-cookie")
    public ResponseEntity<String> readCookie(@CookieValue(name = cookieName, defaultValue = "No cookie") String cookieValue) {
        return ResponseEntity.ok("Cookie Value: " + cookieValue);
    }


    @GetMapping("/read-encrypted-cookie")
    public String readEncryptedCookie(String encryptedValue) throws Exception {
        String decryptedValue = decrypt(encryptedValue);
        return "Decrypted value: " + decryptedValue;
    }


    @GetMapping("/read-cookie-no-default")
    public ResponseEntity<String> readCookieWithNoDefault(@CookieValue(name = cookieName) String cookieValue) {
        return ResponseEntity.ok("Cookie Value: " + cookieValue);
    }

    @GetMapping("/remove-cookie")
    public ResponseEntity<?> removeCookie(HttpServletResponse response){
        Cookie cookie = new Cookie(cookieName,null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        return ResponseEntity.ok("Cookie Removed");
    }
}

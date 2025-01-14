package org.example.proj.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-10
 */

@Component
@Slf4j
public class JwtProvider {

    @Value("${auth.jwt.auth-exp-minutes}")
    private long JWT_AUTH_EXP_MINUTES;

    @Value("${auth.jwt.secret-key}")
    private String JWT_SECRET_KEY;
    @Value("${auth.jwt.issuer}")
    private String JWT_ISSUER;

    private Algorithm algorithm;

    private JWTVerifier jwtVerifier;


    @PostConstruct
    public void init(){
        algorithm = Algorithm.HMAC256(JWT_SECRET_KEY);
        jwtVerifier = JWT.require(algorithm).build();
    }


    private Date getExpiryDate(){
        return Date.from(Instant.now().plus(JWT_AUTH_EXP_MINUTES, ChronoUnit.MINUTES));
    }




    public String createToken(UUID userId){
        return JWT.create()
            .withSubject(userId.toString())
            .withExpiresAt(getExpiryDate())
            .withIssuedAt(Instant.now())
            .withIssuer(JWT_ISSUER)
            .sign(algorithm);
    }



    public JwtVerifyResult verifyToken(String token){


        JwtVerifyResult jwtVerifyResult = JwtVerifyResult.builder()
            .verified(false)
            .decoded(false)
            .build();



        try{
            DecodedJWT result = jwtVerifier.verify(token);
            String subject = result.getSubject();
            UUID userId = UUID.fromString(subject);

            // Valid Token
            jwtVerifyResult.setUserId(userId);
            jwtVerifyResult.setSubject(subject);
            jwtVerifyResult.setExpiresAt(result.getExpiresAt());
            jwtVerifyResult.setVerified(true);
            jwtVerifyResult.setDecoded(true);

        } catch (JWTVerificationException e) {
            try {

                // Expired Token
                DecodedJWT result = JWT.decode(token);
                String subject = result.getSubject();
                UUID userId = UUID.fromString(subject);
                jwtVerifyResult.setSubject(subject);
                jwtVerifyResult.setUserId(userId);
                jwtVerifyResult.setExpiresAt(result.getExpiresAt());
                jwtVerifyResult.setDecoded(true);
            } catch (JWTDecodeException ex) {
                log.info("JWT Decoding failed",ex);
            }
        } catch(IllegalArgumentException e){
            log.info("UUID parsing failed",e);
        } catch(Exception e){
            log.info("JWT Verification failed",e);
        }


        return jwtVerifyResult;
    }



}

package org.example.proj.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.proj.dto.res.SignInFailureResponse;
import org.example.proj.dto.res.SignInSuccessResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * @author rival
 * @since 2024-12-12
 */


@Component
@RequiredArgsConstructor
public class ResponseProvider {

    private final ObjectMapper objectMapper;

    public void sendJsonBody(HttpServletResponse response, int statusCode, Object body) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(statusCode);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(body));
    }


    public void sendSignInSuccessResponse(HttpServletResponse response, UUID userId, String token) throws IOException {
        SignInSuccessResponse body = SignInSuccessResponse.builder()
            .token(token)
            .userId(userId).build();
        this.sendJsonBody(response, HttpServletResponse.SC_OK, body);
    }


    public void sendSignInFailureResponse(HttpServletResponse response) throws IOException {
        SignInFailureResponse body = new SignInFailureResponse();
        this.sendJsonBody(response, HttpServletResponse.SC_BAD_REQUEST,body);
    }


    public void sendSignInFailureResponse(HttpServletResponse response, String message) throws IOException {
        SignInFailureResponse body = new SignInFailureResponse();
        body.setMessage(message);
        this.sendJsonBody(response, HttpServletResponse.SC_BAD_REQUEST,body);
    }
}

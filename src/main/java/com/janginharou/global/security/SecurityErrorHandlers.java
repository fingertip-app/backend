package com.janginharou.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.janginharou.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityErrorHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        write(response, HttpServletResponse.SC_UNAUTHORIZED, ApiResponse.error("UNAUTHORIZED", "Authentication is required"));
    }

    @Override
    public void handle(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN, ApiResponse.error("FORBIDDEN", "Access is denied"));
    }

    private void write(HttpServletResponse response, int status, ApiResponse<Void> body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}

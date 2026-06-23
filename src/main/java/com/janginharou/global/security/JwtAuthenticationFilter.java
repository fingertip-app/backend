package com.janginharou.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.janginharou.global.common.ApiResponse;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.config.JwtTokenProvider;
import com.janginharou.global.config.JwtTokenProvider.AuthTokenClaims;
import com.janginharou.global.exception.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);
        try {
            if (token != null) {
                authenticate(token);
            }
        } catch (JwtException | IllegalArgumentException | UnauthorizedException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), ApiResponse.error("TOKEN_INVALID", "Invalid or expired token"));
            return;
        }
        filterChain.doFilter(request, response);
    }

    protected void authenticate(String token) {
        AuthTokenClaims claims = jwtTokenProvider.parseToken(token);
        String supabaseId = claims.subject();
        if (!StringUtils.hasText(supabaseId)) {
            throw new UnauthorizedException("JWT subject is required");
        }

        String emailClaim = claims.getString("email");
        String email = StringUtils.hasText(emailClaim) ? emailClaim : supabaseId + "@supabase.local";
        String nickname = firstNonBlank(
                claims.getString("nickname"),
                claims.getString("name"),
                email.split("@")[0]
        );

        User user = userRepository.findByProviderId(supabaseId)
                .orElseGet(() -> {
                    String name = claims.getString("name");
                    return userRepository.save(User.supabaseUser(supabaseId, email, nickname, name != null ? name : nickname));
                });

        AuthenticatedUser principal = new AuthenticatedUser(
                user.getId(),
                user.getProviderId(),
                user.getEmail(),
                user.getRole()
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                token,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "사용자";
    }
}

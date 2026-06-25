package com.janginharou.global.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final JwtDecoder supabaseJwtDecoder;
    private final long jwtExpiration;

    public JwtTokenProvider(@Value("${supabase.jwt.secret:${jwt.secret}}") String jwtSecret,
                           @Value("${supabase.url:}") String supabaseUrl,
                           @Value("${jwt.expiration}") long jwtExpiration) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.supabaseJwtDecoder = createSupabaseJwtDecoder(supabaseUrl);
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return parseToken(token).subject();
    }

    public AuthTokenClaims parseToken(String token) {
        if (isAsymmetricSupabaseToken(token)) {
            if (supabaseJwtDecoder == null) {
                throw new JwtException("SUPABASE_URL is required to verify ES256 Supabase JWTs");
            }
            try {
                Jwt jwt = supabaseJwtDecoder.decode(token);
                return new AuthTokenClaims(jwt.getSubject(), jwt.getClaims());
            } catch (org.springframework.security.oauth2.jwt.JwtException e) {
                throw new JwtException(e.getMessage(), e);
            }
        }

        Claims claims = parseLegacyClaims(token);
        return new AuthTokenClaims(claims.getSubject(), claims);
    }

    public Claims parseClaims(String token) {
        return parseLegacyClaims(token);
    }

    private Claims parseLegacyClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private JwtDecoder createSupabaseJwtDecoder(String supabaseUrl) {
        if (!StringUtils.hasText(supabaseUrl)) {
            return null;
        }
        String jwksUri = supabaseUrl.replaceAll("/+$", "") + "/auth/v1/.well-known/jwks.json";
        return NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .build();
    }

    private boolean isAsymmetricSupabaseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return false;
            }
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            return headerJson.contains("\"alg\":\"ES256\"");
        } catch (Exception e) {
            return false;
        }
    }

    public record AuthTokenClaims(String subject, Map<String, Object> claims) {
        public String getString(String name) {
            // 중첩된 필드 지원 (예: "user_metadata.nickname")
            if (name.contains(".")) {
                String[] parts = name.split("\\.", 2);
                Object parent = claims.get(parts[0]);
                if (parent instanceof Map<?, ?> parentMap) {
                    Object value = parentMap.get(parts[1]);
                    return value instanceof String stringValue ? stringValue : null;
                }
                return null;
            }

            Object value = claims.get(name);
            return value instanceof String stringValue ? stringValue : null;
        }
    }
}

package com.smarthas.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Cria e valida tokens JWT (HS256). */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${smarthas.jwt.secret}") String secret,
            @Value("${smarthas.jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** Gera um token cujo "subject" e o e-mail do usuario. */
    public String generateToken(String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /** Extrai o e-mail (subject) de um token valido. */
    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    /** Retorna true se o token for valido e nao expirado. */
    public boolean isValid(String token) {
        try {
            Claims claims = parse(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

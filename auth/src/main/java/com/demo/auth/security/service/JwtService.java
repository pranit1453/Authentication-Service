package com.demo.auth.security.service;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.models.dtos.AuthUser;
import com.demo.auth.models.enums.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    void init() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // Generate JWT token
    public String generateToken(AuthUser user) {
        return Jwts.builder()
                .subject(user.id().toString())
                .claim("email", user.email())
                .claim("roles", user.roles())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Date getExpirationDate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    // Extract user from token
    public AuthUser extractUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getExpiration().before(new Date()))
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token expired");

        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);

        var rolesList = (java.util.List<?>) claims.get("roles");

        Set<RoleType> roles = rolesList.stream()
                .map(Object::toString)
                .map(RoleType::valueOf)
                .collect(Collectors.toSet());

        return new AuthUser(
                userId,
                email,
                roles);
    }

}

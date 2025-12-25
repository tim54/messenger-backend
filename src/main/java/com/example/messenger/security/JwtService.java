
package com.example.messenger.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    // 256-bit base64 secret for HS256. Replace in prod.
    private static final String SECRET_B64 = "bWVzc2VuZ2VyLXNlY3JldC1rZXktMzItYnl0ZXMtbG9uZy1iYXNlNjQ=";
    private Key key() { return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_B64)); }

    public String generateAccessToken(UUID userId, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId.toString())
                .addClaims(Map.of("username", username, "typ", "access"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(900))) // 15m
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId.toString())
                .addClaims(Map.of("username", username, "typ", "refresh"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(60L*60*24*7))) // 7d
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public io.jsonwebtoken.Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
    }
}

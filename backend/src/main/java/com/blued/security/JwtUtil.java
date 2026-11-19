package com.blued.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${blued.jwt.secret}") String secret,
                   @Value("${blued.jwt.expire-hours:8}") long expireHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireHours * 3600_000L;
    }

    public String generate(LoginUser user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("realName", user.getRealName())
                .claim("roles", String.join(",", user.getRoles() == null ? Set.of() : user.getRoles()))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    public LoginUser parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        LoginUser u = new LoginUser();
        u.setId(Long.valueOf(c.getSubject()));
        u.setUsername(c.get("username", String.class));
        u.setRealName(c.get("realName", String.class));
        String roles = c.get("roles", String.class);
        Set<String> set = new HashSet<>();
        if (roles != null && !roles.isBlank()) {
            set.addAll(List.of(roles.split(",")));
        }
        u.setRoles(set);
        return u;
    }
}

package com.donations.donations.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs:3600000}")
    private int jwtExpirationMs; // Default: 1 hour for Access Token

    private static final long REFRESH_TOKEN_MAX_AGE_SECONDS = 60L * 24 * 60 * 60; // 60 days

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "donations_jwt");
        return cookie != null ? cookie.getValue() : null;
    }

    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "donations_refresh_token");
        return cookie != null ? cookie.getValue() : null;
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        return generateJwtCookie(userPrincipal, null);
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal, UUID sessionId) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername(), sessionId);
        return ResponseCookie.from("donations_jwt", jwt)
                .path("/")
                .maxAge(jwtExpirationMs / 1000)
                .httpOnly(true)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("donations_refresh_token", refreshToken)
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE_SECONDS)
                .httpOnly(true)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from("donations_jwt", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie getCleanRefreshTokenCookie() {
        return ResponseCookie.from("donations_refresh_token", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .sameSite("Lax")
                .build();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public UUID getSessionIdFromJwtToken(String token) {
        try {
            Object sid = Jwts.parser().verifyWith(key()).build()
                    .parseSignedClaims(token).getPayload().get("sessionId");
            if (sid != null) {
                return UUID.fromString(sid.toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key()).build().parse(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // log error
        }
        return false;
    }

    private String generateTokenFromUsername(String username, UUID sessionId) {
        var builder = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key());

        if (sessionId != null) {
            builder.claim("sessionId", sessionId.toString());
        }

        return builder.compact();
    }
}

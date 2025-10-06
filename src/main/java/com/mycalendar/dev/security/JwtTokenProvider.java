package com.mycalendar.dev.security;

import com.mycalendar.dev.entity.Role;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtTokenProvider {

    private final UserRepository userRepository;
    private final Set<String> revokedTokens = new HashSet<>();
    private final Set<String> revokedRefreshTokens = new HashSet<>();

    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app.access.token.expiration.milliseconds}")
    private long expiresIn;
    @Value("${app.refresh.token.expiration.milliseconds}")
    private long refreshExpiresIn;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User", "username", userDetails.getUsername()));

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expiresIn);

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(UUID.randomUUID().toString())
                .setIssuer("https://jwt.io")
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .claim("user_id", user.getUserId())
                .claim("username", user.getUsername())
                .claim("roles", roleNames)
                .claim("email", user.getEmail())
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(UUID.randomUUID().toString())
                .setIssuer("https://jwt.io")
                .claim("user_id", userDetails.getUsername())
                .claim("username", userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiresIn))
                .signWith(key())
                .compact();
    }

    public String generateResetPasswordToken(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User", "username", userDetails.getUsername()));

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + 900000);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(UUID.randomUUID().toString())
                .setIssuer("https://jwt.io")
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .claim("user_id", user.getUserId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .signWith(key())
                .compact();
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("username", String.class);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        if (isRefreshTokenRevoked(token)) {
            throw new JwtException("JWT token is revoked");
        }
        return claims.get("username", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException ex) {
            throw new MalformedJwtException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            throw new ExpiredJwtException(null, null, "JWT token is expired");
        } catch (UnsupportedJwtException ex) {
            throw new UnsupportedJwtException("JWT token is unsupported");
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("JWT claims string is empty");
        } catch (JwtException ex) {
            if (ex instanceof SignatureException && isRefreshTokenRevoked(token)) {
                throw new JwtException("JWT token is revoked");
            } else {
                throw new JwtException("JWT token is invalid");
            }
        }
    }

    public boolean isResetPasswordExpired(String accessToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
        return claims.getExpiration().before(new Date());
    }

    public void revokeToken(String accessToken) {
        revokedTokens.add(accessToken);
    }

    public boolean isTokenRevoked(String accessToken) {
        return revokedTokens.contains(accessToken);
    }

    public void revokeRefreshToken(String refreshToken) {
        revokedRefreshTokens.add(refreshToken);
    }

    public boolean isRefreshTokenRevoked(String refreshToken) {
        return revokedRefreshTokens.contains(refreshToken);
    }
}
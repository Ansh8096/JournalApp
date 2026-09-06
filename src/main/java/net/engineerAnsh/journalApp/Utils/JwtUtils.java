package net.engineerAnsh.journalApp.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String secretKey;

    private SecretKey signingKey;

    @PostConstruct
    void initialize() {

        if (
                secretKey == null
                        || secretKey.isBlank()
        ) {

            throw new IllegalStateException(
                    "JWT_SECRET must be configured."
            );
        }

        /*
         * HS256 requires a sufficiently strong key.
         *
         * Keep JWT_SECRET at least 32 bytes long.
         */
        if (
                secretKey.getBytes(
                        StandardCharsets.UTF_8
                ).length < 32
        ) {

            throw new IllegalStateException(
                    "JWT_SECRET must contain at least 32 bytes."
            );
        }

        signingKey =
                Keys.hmacShaKeyFor(
                        secretKey.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    public String extractUsername(
            String token
    ) {

        return extractAllClaims(
                token
        ).getSubject();
    }

    public Date extractExpiration(
            String token
    ) {

        return extractAllClaims(
                token
        ).getExpiration();
    }

    private Claims extractAllClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(
                        signingKey
                )
                .build()
                .parseSignedClaims(
                        token
                )
                .getPayload();
    }

    private boolean isTokenExpired(
            String token
    ) {

        Date expiration =
                extractExpiration(
                        token
                );

        return expiration == null
                || expiration.before(
                new Date()
        );
    }

    public String generateToken(
            String username
    ) {

        Map<String, Object> claims =
                new HashMap<>();

        return createToken(
                claims,
                username
        );
    }

    private String createToken(
            Map<String, Object> claims,
            String subject
    ) {

        Date now =
                new Date();

        Date expiration =
                new Date(
                        now.getTime()
                                + 1000L
                                * 60
                                * 60
                );

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(
            String token
    ) {

        if (
                token == null
                        || token.isBlank()
        ) {

            return false;
        }

        try {

            Claims claims =
                    Jwts.parser()
                            .verifyWith(
                                    signingKey
                            )
                            .build()
                            .parseSignedClaims(
                                    token
                            )
                            .getPayload();

            Date expiration =
                    claims.getExpiration();

            return expiration != null
                    && expiration.after(
                    new Date()
            );

        } catch (
                JwtException
                | IllegalArgumentException ex
        ) {

            return false;
        }
    }
}
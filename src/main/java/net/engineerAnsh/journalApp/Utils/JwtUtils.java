package net.engineerAnsh.journalApp.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    // This is the secret key used to sign and verify your JWTs.
    // Think of it like a password known only to your server.
    // If someone doesn’t have this key, they can’t create valid tokens.
    private String SECRET_KEY = "TaK+HaV^uvCHEFsEVfypW#7g9^k*Z8$V";

    // This converts your secret string into a special format (SecretKey) that the JWT library can use for encryption and signature verification.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Every JWT contains a subject (sub) field — usually the username or user email.
    // This method reads the token, extracts all the data (claims), and returns that username.
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    // This reads when the token will expire — after this time, the token becomes invalid...
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        // we can send information into our payload if we want,
        // with the help of 'claims' (hashMap)...
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // adds custom data (currently empty in our case)...
                .subject(subject) // here subject: username , used for identification ...
                .header().empty().add("typ","JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis())) // it means when the token was created...
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 60 minutes expiration time set for each token...
                .signWith(getSigningKey()) // → uses your secret key to secure the token...
                .compact(); //  creates the final JWT string ...
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

}

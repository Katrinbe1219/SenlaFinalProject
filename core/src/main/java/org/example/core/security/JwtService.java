package org.example.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private static final Logger logger = LogManager.getLogger(JwtService.class);

    @Value("${secret}")
    String secret;
    private final Long expiration = 900_000L;

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Authentication auth) throws Exception {
        try {
            User user = (User) auth.getPrincipal();

            return Jwts.builder()
                    .subject(user.getUsername())
                    .claim("role", auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()))
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusMillis(expiration )))
                    .signWith(getSecretKey(), Jwts.SIG.HS256)
                    .compact();
        }catch (Exception e){
            logger.error("JetService generateToken: " + e.getMessage());
            throw new Exception(e);
        }

    }

    public String generateToken(User user){
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expiration * 1000)))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token){
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token){
        try{
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

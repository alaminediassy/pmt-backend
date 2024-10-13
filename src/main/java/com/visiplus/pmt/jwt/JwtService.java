package com.visiplus.pmt.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.visiplus.pmt.enums.Role;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final String secret;
    private final Long expiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    // Generate token
    // Generate token with userId
    public String generateToken(String email, Long userId, List<Role> roles) {
        List<String> roleNames = roles.stream().map(Enum::name).collect(Collectors.toList());

        return JWT.create()
                .withSubject(email)
                .withClaim("userId", userId)  // Inclure le userId dans le token
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .withClaim("roles", roleNames)
                .sign(Algorithm.HMAC256(secret));
    }

    // Décoder et vérifier un token JWT
    public DecodedJWT decodeToken(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
    }
}

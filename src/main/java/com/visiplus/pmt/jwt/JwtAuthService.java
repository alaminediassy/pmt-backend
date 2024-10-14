package com.visiplus.pmt.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class JwtAuthService {
    private final JwtService jwtService;
    private final JwtBlacklistService jwtBlacklistService;

    public JwtAuthService(JwtService jwtService, JwtBlacklistService jwtBlacklistService) {
        this.jwtService = jwtService;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    // Vérifier si le token est valide et non blacklisté
    public ResponseEntity<String> verifyToken(String token) {
        try {
            String cleanedToken = token.replace("Bearer ", "");  // Retirer le préfixe "Bearer "

            // Vérifier si le token est blacklisté
            if (jwtBlacklistService.isTokenBlacklisted(cleanedToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token is blacklisted");
            }

            // Décoder le token pour vérifier sa validité
            DecodedJWT decodedJWT = jwtService.decodeToken(cleanedToken);

            // Si la vérification est OK, retourner null (aucune erreur)
            return null;

        } catch (Exception e) {
            // En cas d'erreur (token expiré, signature incorrecte, etc.)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
    }
}

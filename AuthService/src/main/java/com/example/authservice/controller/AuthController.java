package com.example.authservice.controller;


import com.example.authservice.dto.CredentialsDto;
import com.example.authservice.mapper.CredentialsMapper;
import com.example.authservice.model.Credentials;
import com.example.authservice.security.JwtService;
import com.example.authservice.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.example.authservice.client.UserClient;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CredentialsService credentialsService;
    private final CredentialsMapper credentialsMapper;
    private final JwtService jwtService;
    private final UserClient userClient;

    /**
     * Endpoint pentru inregistrarea unui utilizator nou
     * Exemplu request:
     * {
     *   "email": "user@example.com",
     *   "password": "1234",
     *   "firstName": "John",
     *   "lastName": "Doe"
     * }
     * Rolul este setat automat la USER de catre server.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");

            // 1) Creează user în UserMicroservice (endpoint public)
            UserClient.UserDto createdUser = userClient.createPublicUser(email, firstName, lastName);
            Long userId = createdUser != null ? createdUser.getId() : null;
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User service did not return an id"));
            }

            // 2) Creează credențiale cu rol implicit USER
            Credentials saved = credentialsService.registerCredentials(userId, email, password);
            CredentialsDto dto = credentialsMapper.toDto(saved);

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "user", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint pentru autentificare (login)
     * Exemplu request:
     * {
     *   "email": "user@example.com",
     *   "password": "1234"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        boolean valid = credentialsService.validateCredentials(email, password);
        if (!valid) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String role = credentialsService.getRoleByEmail(email);
        Long userId = credentialsService.findByEmail(email).map(Credentials::getUserId).orElse(null);
        String token = jwtService.generateToken(email, role, userId);

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "email", email,
                "role", role,
                "userId", userId,
                "token", token
        ));
    }

    /**
     * Endpoint pentru validarea token-ului JWT
     * Utilizat de Traefik ForwardAuth pentru a valida token-urile la alte servicii
     */
    @RequestMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
        }
        String token = authHeader.substring(7);
        try {
            if (jwtService.isTokenExpired(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Token expired"));
            }
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            Long userId = jwtService.extractUserId(token);
            return ResponseEntity.ok()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .header("X-User-Id", userId != null ? userId.toString() : "")
                    .body(Map.of("message", "Token valid"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }
}

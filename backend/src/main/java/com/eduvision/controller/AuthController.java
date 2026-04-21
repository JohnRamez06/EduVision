package com.eduvision.controller;

import com.eduvision.dto.auth.LoginRequest;
import com.eduvision.dto.auth.LoginResponse;
import com.eduvision.dto.auth.RegisterRequest;
import com.eduvision.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/v1/auth/login
     * Body: { "email": "...", "password": "..." }
     * Returns: JWT token + user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/auth/register
     * Body: email, password, firstName, lastName, roleName
     *       + optional role-specific fields (studentNumber, department, etc.)
     * Returns: JWT token + user info (auto-logged in after registration)
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/auth/logout
     * JWT is stateless — the client must delete the token locally.
     * This endpoint exists for audit logging and future token blacklist support.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Stateless: no server-side session to invalidate.
        // Client deletes the token from localStorage / cookie.
        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully. Please delete your token."));
    }
}
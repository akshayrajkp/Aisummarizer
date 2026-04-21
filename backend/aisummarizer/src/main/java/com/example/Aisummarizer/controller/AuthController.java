package com.example.Aisummarizer.controller;

import com.example.Aisummarizer.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService            authService;
    private final AuthenticationManager  authenticationManager;

    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager) {
        this.authService           = authService;
        this.authenticationManager = authenticationManager;
    }

    // ── DTOs ────────────────────────────────────────────────────────
    record RegisterRequest(
        @Email(message = "Must be a valid email")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
    ) {}

    record TokenRequest(
        @NotBlank(message = "Email is required")   String email,
        @NotBlank(message = "Password is required") String password
    ) {}

    // ── POST /auth/register ─────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req.email(), req.password());
        return ResponseEntity.ok(Map.of("message", "Registration successful"));
    }

    // ── POST /auth/token ────────────────────────────────────────────
    @PostMapping("/token")
    public ResponseEntity<?> token(@Valid @RequestBody TokenRequest req) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                .body(Map.of("detail", "Invalid email or password."));
        }

        UserDetails user  = authService.loadUserByUsername(req.email());
        String      token = authService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }
}

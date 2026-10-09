package com.donations.donations.controller;

import com.donations.donations.dto.LoginCommand;
import com.donations.donations.dto.SignupCommand;
import com.donations.donations.service.*;
import com.donations.donations.model.User;
import com.donations.donations.security.UserDetailsImpl;
import com.donations.donations.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@org.springframework.security.core.annotation.AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            User user = getCurrentUserUseCase.execute(userDetails.getId());
            return ResponseEntity.ok(java.util.Map.of("id", user.getId().toString(), "email", user.getEmail(), "role", user.getRole().name()));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(@RequestBody LoginCommand command) {
        ResponseCookie jwtCookie = loginUseCase.execute(command);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(new MessageResponse("Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody SignupCommand command) {
        try {
            registerUserUseCase.execute(command);
            return ResponseEntity.ok(new MessageResponse("User registered! Please check email."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verify(@RequestParam String token) {
        try {
            verifyEmailUseCase.execute(token);
            return ResponseEntity.ok(new MessageResponse("Email verified successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        ResponseCookie cookie = logoutUseCase.execute();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new MessageResponse("Logged out successfully!"));
    }
}

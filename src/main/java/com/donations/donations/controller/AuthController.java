package com.donations.donations.controller;

import com.donations.donations.dto.ForgotPasswordCommand;
import com.donations.donations.dto.LoginCommand;
import com.donations.donations.dto.ResetPasswordCommand;
import com.donations.donations.dto.SignupCommand;
import com.donations.donations.service.*;
import com.donations.donations.model.User;
import com.donations.donations.model.Creator;
import com.donations.donations.security.UserDetailsImpl;
import com.donations.donations.dto.MessageResponse;
import com.donations.donations.repository.CreatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final CreatorRepository creatorRepository;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@org.springframework.security.core.annotation.AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            User user = getCurrentUserUseCase.execute(userDetails.getId());

            String fullName = null;
            String username = null;

            try {
                Optional<Creator> creatorOpt = creatorRepository.findByUserId(user.getId());
                if (creatorOpt.isPresent()) {
                    Creator c = creatorOpt.get();
                    if (c.getFullName() != null && !c.getFullName().isBlank()) {
                        fullName = c.getFullName();
                    }
                    if (c.getUsername() != null && !c.getUsername().isBlank()) {
                        username = c.getUsername();
                    }
                }
            } catch (Exception ignored) {
            }

            String displayName = (fullName != null && !fullName.isBlank())
                    ? fullName
                    : ((username != null && !username.isBlank()) ? username : user.getEmail().split("@")[0]);

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId().toString());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            response.put("name", displayName);
            response.put("fullName", fullName != null ? fullName : displayName);
            if (username != null) {
                response.put("username", username);
            }

            return ResponseEntity.ok(response);
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

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordCommand command) {
        try {
            forgotPasswordUseCase.execute(command);
            return ResponseEntity.ok(new MessageResponse("Se o email estiver registado, enviámos instruções para redefinir a palavra-passe."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordCommand command) {
        try {
            resetPasswordUseCase.execute(command);
            return ResponseEntity.ok(new MessageResponse("Palavra-passe redefinida com sucesso! Já pode iniciar sessão."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}

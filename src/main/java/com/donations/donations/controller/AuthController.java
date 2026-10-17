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

import com.donations.donations.security.JwtUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    private final GoogleAuthUseCase googleAuthUseCase;
    private final UserSessionService userSessionService;
    private final JwtUtils jwtUtils;
    private final com.donations.donations.repository.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${google.clientId:}")
    private String googleClientId;

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
    public ResponseEntity<MessageResponse> login(@RequestBody LoginCommand command, jakarta.servlet.http.HttpServletRequest request) {
        var cookies = loginUseCase.execute(command, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.jwtCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookies.refreshCookie().toString())
                .body(new MessageResponse("Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(jakarta.servlet.http.HttpServletRequest request) {
        String refreshToken = jwtUtils.getRefreshTokenFromCookies(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).body(new MessageResponse("Refresh token não encontrado."));
        }

        Optional<com.donations.donations.model.UserSession> sessionOpt = userSessionService.validateAndTouch(refreshToken);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, jwtUtils.getCleanJwtCookie().toString())
                    .header(HttpHeaders.SET_COOKIE, jwtUtils.getCleanRefreshTokenCookie().toString())
                    .body(new MessageResponse("Sessão expirada ou revogada."));
        }

        var session = sessionOpt.get();
        User user = userRepository.findById(session.getUserId()).orElse(null);
        if (user == null || !user.isActive()) {
            return ResponseEntity.status(401).body(new MessageResponse("Utilizador inativo ou inexistente."));
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        ResponseCookie newJwtCookie = jwtUtils.generateJwtCookie(userDetails, session.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newJwtCookie.toString())
                .body(new MessageResponse("Sessão renovada com sucesso."));
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
    public ResponseEntity<MessageResponse> logout(jakarta.servlet.http.HttpServletRequest request) {
        String refreshToken = jwtUtils.getRefreshTokenFromCookies(request);
        userSessionService.revokeByRefreshToken(refreshToken);

        ResponseCookie cookie = logoutUseCase.execute();
        ResponseCookie cleanRefresh = jwtUtils.getCleanRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, cleanRefresh.toString())
                .body(new MessageResponse("Logged out successfully!"));
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetailsImpl userDetails,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        String jwt = jwtUtils.getJwtFromCookies(request);
        UUID currentSessionId = jwt != null ? jwtUtils.getSessionIdFromJwtToken(jwt) : null;

        var sessions = userSessionService.listActiveSessions(userDetails.getId(), currentSessionId);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> revokeSession(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        userSessionService.revokeSession(id, userDetails.getId());

        String jwt = jwtUtils.getJwtFromCookies(request);
        UUID currentSessionId = jwt != null ? jwtUtils.getSessionIdFromJwtToken(jwt) : null;

        var responseBuilder = ResponseEntity.ok();
        if (currentSessionId != null && currentSessionId.equals(id)) {
            responseBuilder.header(HttpHeaders.SET_COOKIE, jwtUtils.getCleanJwtCookie().toString())
                    .header(HttpHeaders.SET_COOKIE, jwtUtils.getCleanRefreshTokenCookie().toString());
        }

        return responseBuilder.body(new MessageResponse("Sessão terminada com sucesso."));
    }

    @DeleteMapping("/sessions/other")
    public ResponseEntity<?> revokeOtherSessions(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetailsImpl userDetails,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        String jwt = jwtUtils.getJwtFromCookies(request);
        UUID currentSessionId = jwt != null ? jwtUtils.getSessionIdFromJwtToken(jwt) : null;

        userSessionService.revokeOtherSessions(userDetails.getId(), currentSessionId);
        return ResponseEntity.ok(new MessageResponse("Todas as outras sessões foram encerradas."));
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

    @GetMapping("/google/config")
    public ResponseEntity<Map<String, String>> getGoogleConfig() {
        return ResponseEntity.ok(Map.of("clientId", googleClientId != null ? googleClientId : ""));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody com.donations.donations.dto.GoogleAuthCommand command, jakarta.servlet.http.HttpServletRequest request) {
        try {
            GoogleAuthUseCase.GoogleAuthResult result = googleAuthUseCase.execute(command, request);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", result.user().getId().toString());
            userMap.put("email", result.user().getEmail());
            userMap.put("role", result.user().getRole().name());
            userMap.put("name", result.creator() != null && result.creator().getFullName() != null
                    ? result.creator().getFullName()
                    : result.user().getEmail().split("@")[0]);
            if (result.creator() != null && result.creator().getUsername() != null) {
                userMap.put("username", result.creator().getUsername());
            }

            Map<String, Object> body = new HashMap<>();
            body.put("message", "Autenticação com o Google realizada com sucesso.");
            body.put("user", userMap);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                    .header(HttpHeaders.SET_COOKIE, result.refreshCookie().toString())
                    .body(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Erro ao processar autenticação com o Google: " + e.getMessage()));
        }
    }
}

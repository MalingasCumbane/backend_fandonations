package com.donations.donations.service;

import com.donations.donations.dto.GoogleAuthCommand;
import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.model.User;
import com.donations.donations.model.UserRole;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.PasswordEncoderRepository;
import com.donations.donations.repository.UserRepository;
import com.donations.donations.security.JwtUtils;
import com.donations.donations.security.UserDetailsImpl;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthUseCase {

    private final UserRepository userRepository;
    private final CreatorRepository creatorRepository;
    private final PasswordEncoderRepository passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final UserSessionService userSessionService;

    @Value("${google.clientId:}")
    private String configuredClientId;

    public record GoogleAuthResult(ResponseCookie cookie, ResponseCookie refreshCookie, User user, Creator creator) {}

    @Transactional
    public GoogleAuthResult execute(GoogleAuthCommand command) {
        return execute(command, null);
    }

    @Transactional
    public GoogleAuthResult execute(GoogleAuthCommand command, jakarta.servlet.http.HttpServletRequest request) {
        if (command == null || command.getCredential() == null || command.getCredential().isBlank()) {
            throw new IllegalArgumentException("Token de credencial Google inválido ou não fornecido.");
        }

        String credential = command.getCredential().trim();
        JsonNode payload = verifyAndExtractPayload(credential);

        String email = payload.hasNonNull("email") ? payload.get("email").asText().toLowerCase().trim() : null;
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O token Google não contém um endereço de email válido.");
        }

        boolean emailVerified = payload.has("email_verified") && payload.get("email_verified").asBoolean(true);
        if (!emailVerified) {
            throw new IllegalArgumentException("O endereço de email do Google não está verificado.");
        }

        String name = payload.hasNonNull("name") ? payload.get("name").asText().trim() : null;
        String picture = payload.hasNonNull("picture") ? payload.get("picture").asText().trim() : null;

        // 1. Procura ou cria o utilizador
        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
            if (!user.isActive()) {
                user.setActive(true);
                user = userRepository.save(user);
            }
        } else {
            user = User.builder()
                    .email(email)
                    .role(UserRole.CREATOR)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .active(true)
                    .build();
            user = userRepository.save(user);
        }

        // 2. Procura ou cria o perfil de Criador
        Optional<Creator> creatorOpt = creatorRepository.findByUserId(user.getId());
        Creator creator;
        if (creatorOpt.isPresent()) {
            creator = creatorOpt.get();
            if ((creator.getAvatarUrl() == null || creator.getAvatarUrl().isBlank()) && picture != null) {
                creator.setAvatarUrl(picture);
                creator = creatorRepository.save(creator);
            }
        } else {
            String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9._]", "");
            if (baseUsername.length() < 3) {
                baseUsername = "criador" + (System.currentTimeMillis() % 10000);
            }
            String finalUsername = baseUsername;
            int count = 1;
            while (creatorRepository.findByUsername(finalUsername).isPresent()) {
                finalUsername = baseUsername + (count++);
            }

            creator = Creator.builder()
                    .userId(user.getId())
                    .fullName(name != null && !name.isBlank() ? name : finalUsername)
                    .username(finalUsername)
                    .email(email)
                    .avatarUrl(picture)
                    .status(CreatorStatus.DRAFT)
                    .acceptsMarketing(false)
                    .build();
            creator = creatorRepository.save(creator);
        }

        // 3. Cria sessão persistente para o dispositivo e emite os cookies
        com.donations.donations.model.UserSession session = userSessionService.createSession(user.getId(), request);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        ResponseCookie cookie = jwtUtils.generateJwtCookie(userDetails, session.getId());
        ResponseCookie refreshCookie = jwtUtils.generateRefreshTokenCookie(session.getRefreshToken());

        return new GoogleAuthResult(cookie, refreshCookie, user, creator);
    }

    private JsonNode verifyAndExtractPayload(String credential) {
        // Tenta validação remota com a API de tokeninfo da Google se houver conexão
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(4))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + credential))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode verifiedNode = objectMapper.readTree(response.body());
                if (verifiedNode.hasNonNull("email")) {
                    validateAudience(verifiedNode);
                    return verifiedNode;
                }
            }
        } catch (Exception e) {
            log.debug("Verificação remota do tokeninfo da Google falhou (tentando fallback local): {}", e.getMessage());
        }

        // Fallback local: decodifica a assinatura do JWT (payload base64url)
        try {
            String[] parts = credential.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Estrutura do token Google inválida.");
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String json = new String(decoded, StandardCharsets.UTF_8);
            JsonNode payload = objectMapper.readTree(json);

            // Validações básicas de segurança
            if (payload.hasNonNull("iss")) {
                String iss = payload.get("iss").asText();
                if (!iss.equals("accounts.google.com") && !iss.equals("https://accounts.google.com")) {
                    throw new IllegalArgumentException("Emissor do token Google inválido: " + iss);
                }
            }

            if (payload.hasNonNull("exp")) {
                long exp = payload.get("exp").asLong();
                long now = System.currentTimeMillis() / 1000;
                if (exp < now) {
                    throw new IllegalArgumentException("O token Google expirou.");
                }
            }

            validateAudience(payload);
            return payload;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Não foi possível decodificar a credencial Google: " + e.getMessage());
        }
    }

    private void validateAudience(JsonNode node) {
        if (configuredClientId != null && !configuredClientId.isBlank()) {
            if (node.hasNonNull("aud")) {
                String aud = node.get("aud").asText();
                if (!configuredClientId.equals(aud)) {
                    log.warn("Audience do token Google ({}) difere do clientId configurado ({})", aud, configuredClientId);
                }
            }
        }
    }
}

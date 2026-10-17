package com.donations.donations.service;

import com.donations.donations.dto.SessionDto;
import com.donations.donations.model.UserSession;
import com.donations.donations.repository.UserSessionRepository;
import com.donations.donations.util.DeviceParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository sessionRepository;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public UserSession createSession(UUID userId, HttpServletRequest request) {
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String ip = extractClientIp(request);
        String deviceName = DeviceParser.parse(userAgent);

        String token = generateSecureToken();

        UserSession session = UserSession.builder()
                .userId(userId)
                .refreshToken(token)
                .deviceName(deviceName)
                .ipAddress(ip)
                .userAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent)
                .expiresAt(LocalDateTime.now().plusDays(60))
                .lastActiveAt(LocalDateTime.now())
                .revoked(false)
                .build();

        return sessionRepository.save(session);
    }

    @Transactional
    public Optional<UserSession> validateAndTouch(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Optional.empty();
        }

        return sessionRepository.findByRefreshToken(refreshToken)
                .filter(s -> !s.isRevoked())
                .filter(s -> s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(s -> {
                    s.setLastActiveAt(LocalDateTime.now());
                    return sessionRepository.save(s);
                });
    }

    @Transactional(readOnly = true)
    public List<SessionDto> listActiveSessions(UUID userId, UUID currentSessionId) {
        List<UserSession> sessions = sessionRepository.findAllByUserIdAndRevokedFalseOrderByLastActiveAtDesc(userId);
        return sessions.stream().map(s -> SessionDto.builder()
                .id(s.getId().toString())
                .deviceName(s.getDeviceName() != null ? s.getDeviceName() : "Dispositivo Web")
                .ipAddress(maskIp(s.getIpAddress()))
                .lastActiveAt(s.getLastActiveAt())
                .createdAt(s.getCreatedAt())
                .isCurrent(currentSessionId != null && s.getId().equals(currentSessionId))
                .build()
        ).toList();
    }

    @Transactional
    public void revokeSession(UUID sessionId, UUID userId) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            if (s.getUserId().equals(userId)) {
                s.setRevoked(true);
                sessionRepository.save(s);
            }
        });
    }

    @Transactional
    public void revokeOtherSessions(UUID userId, UUID currentSessionId) {
        if (currentSessionId != null) {
            sessionRepository.revokeAllOtherSessions(userId, currentSessionId);
        } else {
            sessionRepository.revokeAllSessions(userId);
        }
    }

    @Transactional
    public void revokeByRefreshToken(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            sessionRepository.findByRefreshToken(refreshToken).ifPresent(s -> {
                s.setRevoked(true);
                sessionRepository.save(s);
            });
        }
    }

    @Transactional(readOnly = true)
    public boolean isSessionRevoked(UUID sessionId) {
        if (sessionId == null) return false;
        return sessionRepository.findById(sessionId)
                .map(UserSession::isRevoked)
                .orElse(true);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "127.0.0.1";
    }

    private String maskIp(String ip) {
        if (ip == null || ip.isBlank()) return "Desconhecido";
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return "Rede Local";
        }
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***.***";
        }
        return ip;
    }
}

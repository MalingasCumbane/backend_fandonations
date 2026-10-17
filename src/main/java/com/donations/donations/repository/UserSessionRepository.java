package com.donations.donations.repository;

import com.donations.donations.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findAllByUserIdAndRevokedFalseOrderByLastActiveAtDesc(UUID userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.revoked = true WHERE s.userId = :userId AND s.id <> :currentSessionId")
    void revokeAllOtherSessions(@Param("userId") UUID userId, @Param("currentSessionId") UUID currentSessionId);

    @Modifying
    @Query("UPDATE UserSession s SET s.revoked = true WHERE s.userId = :userId")
    void revokeAllSessions(@Param("userId") UUID userId);
}

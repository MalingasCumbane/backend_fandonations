package com.donations.donations.domain.repository;

import com.donations.donations.domain.model.VerificationToken;

import java.util.Optional;

public interface VerificationTokenRepository {
    VerificationToken save(VerificationToken token);
    Optional<VerificationToken> findByToken(String token);
    void delete(VerificationToken token);
}

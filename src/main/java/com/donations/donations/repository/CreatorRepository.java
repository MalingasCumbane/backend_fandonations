package com.donations.donations.repository;

import com.donations.donations.model.Creator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreatorRepository extends JpaRepository<Creator, UUID> {
    Optional<Creator> findByUserId(UUID userId);
    Optional<Creator> findByUsername(String username);
}

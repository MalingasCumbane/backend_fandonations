package com.donations.donations.repository;

import com.donations.donations.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    List<WalletTransaction> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);

    List<WalletTransaction> findByCreatorIdOrderByCreatedAtAsc(UUID creatorId);

    List<WalletTransaction> findAllByOrderByCreatedAtDesc();

    Optional<WalletTransaction> findFirstByCreatorIdOrderByCreatedAtDesc(UUID creatorId);

    long countByCreatorId(UUID creatorId);
}

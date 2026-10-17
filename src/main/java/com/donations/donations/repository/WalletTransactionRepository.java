package com.donations.donations.repository;

import com.donations.donations.model.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID>, JpaSpecificationExecutor<WalletTransaction> {
    List<WalletTransaction> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);
    Page<WalletTransaction> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId, Pageable pageable);

    List<WalletTransaction> findByCreatorIdOrderByCreatedAtAsc(UUID creatorId);

    List<WalletTransaction> findAllByOrderByCreatedAtDesc();
    Page<WalletTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<WalletTransaction> findFirstByCreatorIdOrderByCreatedAtDesc(UUID creatorId);

    long countByCreatorId(UUID creatorId);
}

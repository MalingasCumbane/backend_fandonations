package com.donations.donations.service.wallet;

import com.donations.donations.dto.PageResponse;
import com.donations.donations.dto.creator.WalletTransactionResponseDto;
import com.donations.donations.model.Creator;
import com.donations.donations.model.Donation;
import com.donations.donations.model.Payout;
import com.donations.donations.model.PlatformSettings;
import com.donations.donations.model.WalletTransaction;
import com.donations.donations.model.enums.DonationStatus;
import com.donations.donations.model.enums.PayoutStatus;
import com.donations.donations.model.enums.TransactionCategory;
import com.donations.donations.model.enums.TransactionType;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.DonationRepository;
import com.donations.donations.repository.PayoutRepository;
import com.donations.donations.repository.PlatformSettingsRepository;
import com.donations.donations.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final DonationRepository donationRepository;
    private final PayoutRepository payoutRepository;
    private final CreatorRepository creatorRepository;
    private final PlatformSettingsRepository settingsRepository;

    @Transactional
    public synchronized WalletTransaction recordTransaction(UUID creatorId, TransactionType type, TransactionCategory category, BigDecimal amount, String referenceType, String referenceId, String description, LocalDateTime timestamp) {
        if (walletTransactionRepository.countByCreatorId(creatorId) == 0) {
            backfillHistory(creatorId);
        }

        BigDecimal currentBalance = getCurrentBalance(creatorId);
        BigDecimal balanceAfter = type == TransactionType.CREDIT ? currentBalance.add(amount) : currentBalance.subtract(amount);

        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            balanceAfter = BigDecimal.ZERO;
        }

        WalletTransaction tx = WalletTransaction.builder().creatorId(creatorId).type(type).category(category).amount(amount).balanceAfter(balanceAfter).referenceType(referenceType).referenceId(referenceId).description(description).build();

        if (timestamp != null) {
            tx.setCreatedAt(timestamp);
            tx.setUpdatedAt(timestamp);
        }

        log.info("Recorded wallet transaction for creator {}: type={}, category={}, amount={}, balanceAfter={}", creatorId, type, category, amount, balanceAfter);

        return walletTransactionRepository.save(tx);
    }

    public BigDecimal getCurrentBalance(UUID creatorId) {
        return walletTransactionRepository.findFirstByCreatorIdOrderByCreatedAtDesc(creatorId).map(WalletTransaction::getBalanceAfter).orElse(BigDecimal.ZERO);
    }

    @Transactional
    public List<WalletTransactionResponseDto> getTransactionsForCreator(UUID creatorId) {
        if (walletTransactionRepository.countByCreatorId(creatorId) == 0) {
            backfillHistory(creatorId);
        }

        Creator creator = creatorRepository.findById(creatorId).orElse(null);
        String username = creator != null ? creator.getUsername() : "";
        String fullName = creator != null ? (creator.getFullName() != null ? creator.getFullName() : creator.getUsername()) : "";

        return walletTransactionRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId).stream().map(tx -> toDto(tx, username, fullName)).collect(Collectors.toList());
    }

    @Transactional
    public PageResponse<WalletTransactionResponseDto> getTransactionsForCreatorPaged(UUID creatorId, int page, int size) {
        return getTransactionsForCreatorPaged(creatorId, page, size, null, null, null, null);
    }

    @Transactional
    public PageResponse<WalletTransactionResponseDto> getTransactionsForCreatorPaged(
            UUID creatorId, int page, int size, String type, String category, String search, String period
    ) {
        if (walletTransactionRepository.countByCreatorId(creatorId) == 0) {
            backfillHistory(creatorId);
        }

        Creator creator = creatorRepository.findById(creatorId).orElse(null);
        String username = creator != null ? creator.getUsername() : "";
        String fullName = creator != null ? (creator.getFullName() != null ? creator.getFullName() : creator.getUsername()) : "";

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size));

        boolean hasFilters = (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type))
                || (category != null && !category.isBlank())
                || (search != null && !search.isBlank())
                || (period != null && !period.isBlank() && !"all".equalsIgnoreCase(period));

        org.springframework.data.domain.Page<WalletTransaction> pageResult;

        if (!hasFilters) {
            pageResult = walletTransactionRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId, pageable);
        } else {
            org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = (root, query, cb) -> {
                List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("creatorId"), creatorId));

                if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
                    if ("PLATFORM_FEE".equalsIgnoreCase(type)) {
                        predicates.add(root.get("category").in(
                                TransactionCategory.PLATFORM_FEE,
                                TransactionCategory.PLATFORM_FEE_REFUND
                        ));
                    } else {
                        try {
                            TransactionType txType = TransactionType.valueOf(type.toUpperCase());
                            predicates.add(cb.equal(root.get("type"), txType));
                        } catch (Exception ignored) {}
                    }
                }

                if (category != null && !category.isBlank()) {
                    try {
                        TransactionCategory cat = TransactionCategory.valueOf(category.toUpperCase());
                        predicates.add(cb.equal(root.get("category"), cat));
                    } catch (Exception ignored) {}
                }

                if (period != null && !period.isBlank() && !"all".equalsIgnoreCase(period)) {
                    try {
                        int days = Integer.parseInt(period);
                        LocalDateTime since = LocalDateTime.now().minusDays(days);
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), since));
                    } catch (Exception ignored) {}
                }

                if (search != null && !search.isBlank()) {
                    String pattern = "%" + search.trim().toLowerCase() + "%";
                    var descMatch = cb.like(cb.lower(root.get("description")), pattern);
                    var refMatch = cb.like(cb.lower(root.get("referenceId")), pattern);
                    predicates.add(cb.or(descMatch, refMatch));
                }

                query.orderBy(cb.desc(root.get("createdAt")));
                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            };

            pageResult = walletTransactionRepository.findAll(spec, pageable);
        }

        return PageResponse.of(pageResult, tx -> toDto(tx, username, fullName));
    }

    @Transactional
    public List<WalletTransactionResponseDto> getAllTransactions(UUID optionalCreatorId) {
        if (optionalCreatorId != null) {
            return getTransactionsForCreator(optionalCreatorId);
        }

        List<Creator> allCreators = creatorRepository.findAll();
        Map<UUID, Creator> creatorMap = allCreators.stream().collect(Collectors.toMap(Creator::getId, c -> c));

        for (Creator c : allCreators) {
            if (walletTransactionRepository.countByCreatorId(c.getId()) == 0) {
                backfillHistory(c.getId());
            }
        }

        return walletTransactionRepository.findAllByOrderByCreatedAtDesc().stream().map(tx -> {
            Creator c = creatorMap.get(tx.getCreatorId());
            String username = c != null ? c.getUsername() : "";
            String fullName = c != null ? (c.getFullName() != null ? c.getFullName() : c.getUsername()) : "";
            return toDto(tx, username, fullName);
        }).collect(Collectors.toList());
    }

    @Transactional
    public com.donations.donations.dto.PageResponse<WalletTransactionResponseDto> getAllTransactionsPaged(UUID optionalCreatorId, int page, int size) {
        return getAllTransactionsPaged(optionalCreatorId, page, size, null, null, null, null);
    }

    @Transactional
    public com.donations.donations.dto.PageResponse<WalletTransactionResponseDto> getAllTransactionsPaged(
            UUID optionalCreatorId, int page, int size, String type, String category, String search, String period
    ) {
        if (optionalCreatorId != null) {
            return getTransactionsForCreatorPaged(optionalCreatorId, page, size, type, category, search, period);
        }

        List<Creator> allCreators = creatorRepository.findAll();
        Map<UUID, Creator> creatorMap = allCreators.stream().collect(Collectors.toMap(Creator::getId, c -> c));

        for (Creator c : allCreators) {
            if (walletTransactionRepository.countByCreatorId(c.getId()) == 0) {
                backfillHistory(c.getId());
            }
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size));

        boolean hasFilters = (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type))
                || (category != null && !category.isBlank())
                || (search != null && !search.isBlank())
                || (period != null && !period.isBlank() && !"all".equalsIgnoreCase(period));

        org.springframework.data.domain.Page<WalletTransaction> pageResult;

        if (!hasFilters) {
            pageResult = walletTransactionRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = (root, query, cb) -> {
                List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

                if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
                    if ("PLATFORM_FEE".equalsIgnoreCase(type)) {
                        predicates.add(root.get("category").in(
                                TransactionCategory.PLATFORM_FEE,
                                TransactionCategory.PLATFORM_FEE_REFUND
                        ));
                    } else {
                        try {
                            TransactionType txType = TransactionType.valueOf(type.toUpperCase());
                            predicates.add(cb.equal(root.get("type"), txType));
                        } catch (Exception ignored) {}
                    }
                }

                if (category != null && !category.isBlank()) {
                    try {
                        TransactionCategory cat = TransactionCategory.valueOf(category.toUpperCase());
                        predicates.add(cb.equal(root.get("category"), cat));
                    } catch (Exception ignored) {}
                }

                if (period != null && !period.isBlank() && !"all".equalsIgnoreCase(period)) {
                    try {
                        int days = Integer.parseInt(period);
                        LocalDateTime since = LocalDateTime.now().minusDays(days);
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), since));
                    } catch (Exception ignored) {}
                }

                if (search != null && !search.isBlank()) {
                    String pattern = "%" + search.trim().toLowerCase() + "%";
                    var descMatch = cb.like(cb.lower(root.get("description")), pattern);
                    var refMatch = cb.like(cb.lower(root.get("referenceId")), pattern);

                    List<UUID> matchingCreatorIds = allCreators.stream()
                            .filter(c -> (c.getUsername() != null && c.getUsername().toLowerCase().contains(search.trim().toLowerCase()))
                                    || (c.getFullName() != null && c.getFullName().toLowerCase().contains(search.trim().toLowerCase())))
                            .map(Creator::getId)
                            .collect(Collectors.toList());

                    if (!matchingCreatorIds.isEmpty()) {
                        predicates.add(cb.or(descMatch, refMatch, root.get("creatorId").in(matchingCreatorIds)));
                    } else {
                        predicates.add(cb.or(descMatch, refMatch));
                    }
                }

                query.orderBy(cb.desc(root.get("createdAt")));
                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            };

            pageResult = walletTransactionRepository.findAll(spec, pageable);
        }

        return com.donations.donations.dto.PageResponse.of(pageResult, tx -> {
            Creator c = creatorMap.get(tx.getCreatorId());
            String username = c != null ? c.getUsername() : "";
            String fullName = c != null ? (c.getFullName() != null ? c.getFullName() : c.getUsername()) : "";
            return toDto(tx, username, fullName);
        });
    }

    @Transactional
    public synchronized void backfillHistory(UUID creatorId) {
        if (walletTransactionRepository.countByCreatorId(creatorId) > 0) {
            return;
        }

        List<Donation> donations = donationRepository.findByCreatorId(creatorId).stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS && d.getCreatedAt() != null).collect(Collectors.toList());

        List<Payout> payouts = payoutRepository.findByCreatorId(creatorId).stream().filter(p -> p.getStatus() != PayoutStatus.FAILED && p.getRequestedAt() != null).collect(Collectors.toList());

        if (donations.isEmpty() && payouts.isEmpty()) {
            return;
        }

        BigDecimal feePercentage = settingsRepository.getSettings().map(PlatformSettings::getPlatformFeePercentage).orElse(new BigDecimal("10.00"));

        List<ChronologicalEvent> events = new ArrayList<>();

        for (Donation d : donations) {
            events.add(new ChronologicalEvent(d.getCreatedAt(), "DONATION", d, null));
        }

        for (Payout p : payouts) {
            events.add(new ChronologicalEvent(p.getRequestedAt(), "PAYOUT", null, p));
            if (p.getStatus() == PayoutStatus.CANCELLED && p.getProcessedAt() != null) {
                events.add(new ChronologicalEvent(p.getProcessedAt(), "PAYOUT_CANCELLED", null, p));
            }
        }

        events.sort(Comparator.comparing(ChronologicalEvent::getTimestamp));

        BigDecimal runningBalance = BigDecimal.ZERO;

        for (ChronologicalEvent event : events) {
            if ("DONATION".equals(event.getType())) {
                Donation d = event.getDonation();
                BigDecimal amount = d.getAmount();
                runningBalance = runningBalance.add(amount);

                String supporter = d.isAnonymous() ? "Apoiador Anónimo" : (d.getSupporterName() != null ? d.getSupporterName() : "Apoiador");
                WalletTransaction tx = WalletTransaction.builder().creatorId(creatorId).type(TransactionType.CREDIT).category(TransactionCategory.DONATION_RECEIVED).amount(amount).balanceAfter(runningBalance).referenceType("DONATION").referenceId(d.getReference()).description("Apoio recebido de " + supporter + (d.getMethod() != null ? " (" + d.getMethod().name() + ")" : "")).build();
                tx.setCreatedAt(d.getCreatedAt());
                tx.setUpdatedAt(d.getCreatedAt());
                walletTransactionRepository.save(tx);

            } else if ("PAYOUT".equals(event.getType())) {
                Payout p = event.getPayout();
                BigDecimal payoutAmt = p.getAmount();
                BigDecimal feeAmt = payoutAmt.multiply(feePercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                runningBalance = runningBalance.subtract(payoutAmt);
                if (runningBalance.compareTo(BigDecimal.ZERO) < 0) runningBalance = BigDecimal.ZERO;

                WalletTransaction txPayout = WalletTransaction.builder().creatorId(creatorId).type(TransactionType.DEBIT).category(TransactionCategory.PAYOUT_REQUESTED).amount(payoutAmt).balanceAfter(runningBalance).referenceType("PAYOUT").referenceId(p.getId().toString()).description("Pedido de levantamento para " + (p.getPhone() != null ? p.getPhone() : "") + " (" + (p.getMethod() != null ? p.getMethod().name() : "") + ")").build();
                txPayout.setCreatedAt(p.getRequestedAt());
                txPayout.setUpdatedAt(p.getRequestedAt());
                walletTransactionRepository.save(txPayout);

                runningBalance = runningBalance.subtract(feeAmt);
                if (runningBalance.compareTo(BigDecimal.ZERO) < 0) runningBalance = BigDecimal.ZERO;

                WalletTransaction txFee = WalletTransaction.builder().creatorId(creatorId).type(TransactionType.DEBIT).category(TransactionCategory.PLATFORM_FEE).amount(feeAmt).balanceAfter(runningBalance).referenceType("PAYOUT").referenceId(p.getId().toString()).description("Taxa de serviço da plataforma (" + feePercentage + "%)").build();
                txFee.setCreatedAt(p.getRequestedAt().plusSeconds(1));
                txFee.setUpdatedAt(p.getRequestedAt().plusSeconds(1));
                walletTransactionRepository.save(txFee);

            } else if ("PAYOUT_CANCELLED".equals(event.getType())) {
                Payout p = event.getPayout();
                BigDecimal payoutAmt = p.getAmount();
                BigDecimal feeAmt = payoutAmt.multiply(feePercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                runningBalance = runningBalance.add(payoutAmt);
                WalletTransaction txRefundAmt = WalletTransaction.builder().creatorId(creatorId).type(TransactionType.CREDIT).category(TransactionCategory.PAYOUT_REFUND).amount(payoutAmt).balanceAfter(runningBalance).referenceType("PAYOUT").referenceId(p.getId().toString()).description("Estorno de pedido de levantamento recusado").build();
                txRefundAmt.setCreatedAt(p.getProcessedAt());
                txRefundAmt.setUpdatedAt(p.getProcessedAt());
                walletTransactionRepository.save(txRefundAmt);

                runningBalance = runningBalance.add(feeAmt);
                WalletTransaction txRefundFee = WalletTransaction.builder().creatorId(creatorId).type(TransactionType.CREDIT).category(TransactionCategory.PLATFORM_FEE_REFUND).amount(feeAmt).balanceAfter(runningBalance).referenceType("PAYOUT").referenceId(p.getId().toString()).description("Estorno de taxa da plataforma por levantamento recusado").build();
                txRefundFee.setCreatedAt(p.getProcessedAt().plusSeconds(1));
                txRefundFee.setUpdatedAt(p.getProcessedAt().plusSeconds(1));
                walletTransactionRepository.save(txRefundFee);
            }
        }
    }

    private WalletTransactionResponseDto toDto(WalletTransaction tx, String username, String fullName) {
        return WalletTransactionResponseDto.builder().id(tx.getId()).creatorId(tx.getCreatorId()).creatorUsername(username).creatorFullName(fullName).type(tx.getType()).category(tx.getCategory()).amount(tx.getAmount()).balanceAfter(tx.getBalanceAfter()).referenceType(tx.getReferenceType()).referenceId(tx.getReferenceId()).description(tx.getDescription()).createdAt(tx.getCreatedAt()).build();
    }

    @lombok.Value
    private static class ChronologicalEvent {
        LocalDateTime timestamp;
        String type;
        Donation donation;
        Payout payout;
    }
}

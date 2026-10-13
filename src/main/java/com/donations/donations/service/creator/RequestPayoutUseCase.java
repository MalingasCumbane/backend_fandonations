package com.donations.donations.service.creator;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.DonationRepository;
import com.donations.donations.repository.PayoutRepository;
import com.donations.donations.repository.PlatformSettingsRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.model.Donation;
import com.donations.donations.model.Payout;
import com.donations.donations.model.PlatformSettings;
import com.donations.donations.model.enums.DonationStatus;
import com.donations.donations.model.enums.PaymentMethod;
import com.donations.donations.model.enums.PayoutStatus;
import com.donations.donations.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestPayoutUseCase {
    private final PayoutRepository payoutRepository;
    private final CreatorRepository creatorRepository;
    private final com.donations.donations.repository.UserRepository userRepository;
    private final PlatformSettingsRepository settingsRepository;
    private final DonationRepository donationRepository;
    private final com.donations.donations.service.realtime.RealtimeEventPublisher realtimeEventPublisher;
    private final EmailRepository emailRepository;
    private final com.donations.donations.service.wallet.WalletService walletService;

    @Value("${app.admin.email:mcumbane@speranza.co.mz}")
    private String adminEmail;

    public Payout execute(UUID creatorId, BigDecimal amount, PaymentMethod method, String phone) {
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Criador não encontrado."));

        if (creator.getStatus() != CreatorStatus.VERIFIED) {
            throw new IllegalArgumentException("O criador não está verificado.");
        }

        PlatformSettings settings = settingsRepository.getSettings()
                .orElseThrow(() -> new IllegalStateException("Configurações não encontradas."));

        if (!settings.isPayoutsEnabled()) {
            throw new IllegalArgumentException("Os levantamentos estão temporariamente desativados.");
        }

        if (amount.compareTo(settings.getMinPayoutAmount()) < 0) {
            throw new IllegalArgumentException("O valor é inferior ao mínimo permitido.");
        }

        List<Donation> donations = donationRepository.findByCreatorId(creatorId);
        BigDecimal totalReceived = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Payout> payouts = payoutRepository.findByCreatorId(creatorId);
        BigDecimal paidOut = payouts.stream()
                .filter(p -> p.getStatus() == PayoutStatus.PAID || p.getStatus() == PayoutStatus.PROCESSING || p.getStatus() == PayoutStatus.PENDING)
                .map(Payout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal available = totalReceived.subtract(paidOut);

        BigDecimal feePercentage = settings.getPlatformFeePercentage() != null 
                ? settings.getPlatformFeePercentage() 
                : new BigDecimal("10.00");
        BigDecimal feeAmount = amount.multiply(feePercentage).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalRequired = amount.add(feeAmount);

        if (totalRequired.compareTo(available) > 0) {
            throw new IllegalArgumentException("Saldo insuficiente para cobrir o levantamento (" + amount + " MT) mais a taxa da plataforma (" + feeAmount + " MT). Total necessário: " + totalRequired + " MT.");
        }

        Payout payout = Payout.builder()
                .creatorId(creatorId)
                .amount(amount)
                .method(method)
                .phone(phone)
                .status(PayoutStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        Payout saved = payoutRepository.save(payout);

        // Registar movimentos de débito no Livro-Razão (Ledger da Carteira)
        try {
            walletService.recordTransaction(
                    creatorId,
                    com.donations.donations.model.enums.TransactionType.DEBIT,
                    com.donations.donations.model.enums.TransactionCategory.PAYOUT_REQUESTED,
                    amount,
                    "PAYOUT",
                    saved.getId().toString(),
                    "Pedido de levantamento para " + phone + " (" + method.name() + ")",
                    saved.getRequestedAt()
            );

            walletService.recordTransaction(
                    creatorId,
                    com.donations.donations.model.enums.TransactionType.DEBIT,
                    com.donations.donations.model.enums.TransactionCategory.PLATFORM_FEE,
                    feeAmount,
                    "PAYOUT",
                    saved.getId().toString(),
                    "Taxa de serviço da plataforma (" + feePercentage + "%)",
                    saved.getRequestedAt().plusSeconds(1)
            );
        } catch (Exception e) {
            System.err.println("Erro ao registar débitos na carteira para o levantamento: " + e.getMessage());
        }

        try { realtimeEventPublisher.notifyAdminPayouts(saved); } catch (Exception ignored) {}

        // Enviar email de notificação a todos os administradores da plataforma e ao Criador
        try {
            String dateFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String amountStr = String.format("%,.2f", amount);
            String feeStr = String.format("%,.2f", feeAmount);
            String totalDeductedStr = String.format("%,.2f", totalRequired);

            // Obter todos os emails associados ao criador
            java.util.Set<String> recipientCreatorEmails = new java.util.LinkedHashSet<>();
            if (creator.getEmail() != null && !creator.getEmail().isBlank()) {
                recipientCreatorEmails.add(creator.getEmail().trim());
            }
            if (creator.getUserId() != null) {
                userRepository.findById(creator.getUserId()).ifPresent(u -> {
                    if (u.getEmail() != null && !u.getEmail().isBlank()) {
                        recipientCreatorEmails.add(u.getEmail().trim());
                    }
                });
            }

            String primaryCreatorEmail = recipientCreatorEmails.isEmpty() ? "N/D" : recipientCreatorEmails.iterator().next();

            // 1. Coletar todos os administradores da plataforma
            java.util.Set<String> recipientAdminEmails = new java.util.LinkedHashSet<>();
            if (adminEmail != null && !adminEmail.isBlank()) {
                recipientAdminEmails.add(adminEmail.trim());
            }

            try {
                List<com.donations.donations.model.User> adminUsers = userRepository.findByRoleIn(
                        List.of(
                                com.donations.donations.model.UserRole.SUPER_ADMIN,
                                com.donations.donations.model.UserRole.ADMIN,
                                com.donations.donations.model.UserRole.FINANCE
                        )
                );
                for (com.donations.donations.model.User u : adminUsers) {
                    if (u.getEmail() != null && !u.getEmail().isBlank()) {
                        recipientAdminEmails.add(u.getEmail().trim());
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao buscar administradores: " + e.getMessage());
            }

            // Enviar email para cada administrador
            for (String targetAdmin : recipientAdminEmails) {
                System.out.println("[Email] A enviar notificação de pedido de levantamento para admin: " + targetAdmin);
                emailRepository.sendPayoutRequestedAdminEmail(
                        targetAdmin,
                        creator.getFullName() != null ? creator.getFullName() : creator.getUsername(),
                        creator.getUsername(),
                        primaryCreatorEmail,
                        amountStr,
                        feeStr,
                        totalDeductedStr,
                        method.name(),
                        phone,
                        dateFormatted
                );
            }

            // 2. Email para o Criador confirmando o pedido e avisando do prazo de até 3 dias
            if (recipientCreatorEmails.isEmpty()) {
                System.out.println("[Email] Aviso: email do criador não encontrado (creatorId=" + creatorId + ").");
            } else {
                for (String targetCreator : recipientCreatorEmails) {
                    System.out.println("[Email] A enviar confirmação de levantamento para criador: " + targetCreator);
                    emailRepository.sendPayoutRequestedCreatorEmail(
                            targetCreator,
                            creator.getFullName() != null ? creator.getFullName() : creator.getUsername(),
                            amountStr,
                            method.name(),
                            phone,
                            feeStr,
                            totalDeductedStr,
                            dateFormatted
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar emails de levantamento: " + e.getMessage());
            e.printStackTrace();
        }

        return saved;
    }
}

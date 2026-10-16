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

    @Value("${app.admin.email}")
    private String adminEmail;

    public record PayoutValidationResult(
            Creator creator,
            String digits,
            BigDecimal feeAmount,
            BigDecimal totalRequired,
            PlatformSettings settings
    ) {
    }

    public PayoutValidationResult validatePayoutRequest(UUID creatorId, BigDecimal amount, PaymentMethod method, String phone) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do levantamento é inválido.");
        }
        if (method == null) {
            throw new IllegalArgumentException("O método de pagamento é obrigatório.");
        }

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
            throw new IllegalArgumentException("O valor é inferior ao mínimo permitido (" + settings.getMinPayoutAmount() + " MT).");
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

        // Determinar e validar o número de telefone de destino conforme o método
        String targetPhone = phone;
        if (targetPhone == null || targetPhone.isBlank()) {
            if (method == PaymentMethod.MPESA) {
                targetPhone = creator.getMpesaPhone() != null ? creator.getMpesaPhone() : creator.getPhone();
            } else if (method == PaymentMethod.EMOLA) {
                targetPhone = creator.getEmolaPhone() != null ? creator.getEmolaPhone() : creator.getPhone();
            }
        }

        if (targetPhone == null || targetPhone.isBlank()) {
            throw new IllegalArgumentException("Nenhum número de telefone configurado para o método " + method.name() + ".");
        }

        String digits = targetPhone.replaceAll("\\D", "").replaceFirst("^258", "");
        if (digits.length() != 9) {
            throw new IllegalArgumentException("O número de telemóvel para levantamento deve ter 9 dígitos.");
        }

        if (method == PaymentMethod.MPESA && !(digits.startsWith("84") || digits.startsWith("85"))) {
            throw new IllegalArgumentException("O número selecionado (" + digits + ") não é um número válido da Vodacom M-Pesa (deve começar por 84 ou 85).");
        }

        if (method == PaymentMethod.EMOLA && !(digits.startsWith("86") || digits.startsWith("87"))) {
            throw new IllegalArgumentException("O número selecionado (" + digits + ") não é um número válido da Movitel e-Mola (deve começar por 86 ou 87).");
        }

        // Validação de segurança: garantir que o número é um dos números registados do criador
        String mpesaDigits = creator.getMpesaPhone() != null ? creator.getMpesaPhone().replaceAll("\\D", "").replaceFirst("^258", "") : null;
        String emolaDigits = creator.getEmolaPhone() != null ? creator.getEmolaPhone().replaceAll("\\D", "").replaceFirst("^258", "") : null;
        String generalDigits = creator.getPhone() != null ? creator.getPhone().replaceAll("\\D", "").replaceFirst("^258", "") : null;

        boolean matchesVerified = digits.equals(mpesaDigits) || digits.equals(emolaDigits) || digits.equals(generalDigits);
        if (!matchesVerified) {
            throw new IllegalArgumentException("O número de telefone indicado (" + digits + ") não está verificado na sua conta Txopa.");
        }

        return new PayoutValidationResult(creator, digits, feeAmount, totalRequired, settings);
    }

    public Payout execute(UUID creatorId, BigDecimal amount, PaymentMethod method, String phone) {
        PayoutValidationResult val = validatePayoutRequest(creatorId, amount, method, phone);
        Creator creator = val.creator();
        String digits = val.digits();
        BigDecimal feeAmount = val.feeAmount();
        BigDecimal totalRequired = val.totalRequired();
        BigDecimal feePercentage = val.settings().getPlatformFeePercentage() != null
                ? val.settings().getPlatformFeePercentage()
                : new BigDecimal("10.00");

        Payout payout = Payout.builder()
                .creatorId(creatorId)
                .amount(amount)
                .method(method)
                .phone(digits)
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

        try {
            realtimeEventPublisher.notifyAdminPayouts(saved);
        } catch (Exception ignored) {
        }

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

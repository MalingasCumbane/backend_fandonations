package com.donations.donations.controller;

import com.donations.donations.security.UserDetailsImpl;
import com.donations.donations.service.SendPhoneOtpUseCase;
import com.donations.donations.service.VerifyPhoneOtpUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/creators/me/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final SendPhoneOtpUseCase sendPhoneOtpUseCase;
    private final VerifyPhoneOtpUseCase verifyPhoneOtpUseCase;
    private final com.donations.donations.service.security.SecurityOtpService securityOtpService;
    private final com.donations.donations.repository.CreatorRepository creatorRepository;
    private final com.donations.donations.repository.UserRepository userRepository;
    private final com.donations.donations.repository.EmailRepository emailRepository;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String phoneNumber = body.get("phoneNumber") != null ? body.get("phoneNumber") : body.get("phone");
        String provider = body.get("provider");
        sendPhoneOtpUseCase.execute(userDetails.getId(), phoneNumber, provider);
        return ResponseEntity.ok(Map.of("message", "Código de verificação enviado para o seu email."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String phoneNumber = body.get("phoneNumber") != null ? body.get("phoneNumber") : body.get("phone");
        verifyPhoneOtpUseCase.execute(userDetails.getId(), phoneNumber, body.get("otp"));
        return ResponseEntity.ok(Map.of("message", "Número de telefone validado com sucesso."));
    }

    @PostMapping("/send-remove-otp")
    public ResponseEntity<?> sendRemoveOtp(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String provider = body.get("provider");
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("O provedor (MPESA ou EMOLA) é obrigatório.");
        }

        var creator = creatorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException("Perfil de criador não encontrado."));

        String phone = null;
        if ("MPESA".equalsIgnoreCase(provider)) {
            phone = creator.getMpesaPhone() != null ? creator.getMpesaPhone() : (creator.getPhone() != null && (creator.getPhone().startsWith("84") || creator.getPhone().startsWith("85")) ? creator.getPhone() : null);
        } else if ("EMOLA".equalsIgnoreCase(provider)) {
            phone = creator.getEmolaPhone() != null ? creator.getEmolaPhone() : (creator.getPhone() != null && (creator.getPhone().startsWith("86") || creator.getPhone().startsWith("87")) ? creator.getPhone() : null);
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Nenhum número verificado encontrado para o provedor " + provider + ".");
        }

        String targetInfo = "provider=" + provider.toUpperCase() + ";phone=" + phone;
        String otp = securityOtpService.generateAndSaveOtp(userDetails.getId(), "PHONE_REMOVAL", targetInfo);

        var user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado."));

        emailRepository.sendPhoneRemovalOtpEmail(user.getEmail(), otp, phone, provider.toUpperCase());

        return ResponseEntity.ok(Map.of(
                "message", "Código de confirmação enviado para o seu email.",
                "expiresInMinutes", 10
        ));
    }

    @DeleteMapping("/{provider}")
    public ResponseEntity<?> removePhone(
            @PathVariable String provider,
            @RequestParam(value = "otp", required = false) String otpParam,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String otp = (otpParam != null && !otpParam.isBlank()) ? otpParam : (body != null ? body.get("otp") : null);
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("O código OTP é obrigatório para remover o número verificado.");
        }

        securityOtpService.verifyAndConsumeOtp(userDetails.getId(), "PHONE_REMOVAL", otp);
        verifyPhoneOtpUseCase.removePhone(userDetails.getId(), provider);
        return ResponseEntity.ok(Map.of("message", "Número removido com sucesso."));
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removePhonePost(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String provider = body.get("provider");
        String otp = body.get("otp");
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("O provedor é obrigatório.");
        }
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("O código OTP é obrigatório para remover o número verificado.");
        }

        securityOtpService.verifyAndConsumeOtp(userDetails.getId(), "PHONE_REMOVAL", otp);
        verifyPhoneOtpUseCase.removePhone(userDetails.getId(), provider);
        return ResponseEntity.ok(Map.of("message", "Número removido com sucesso."));
    }
}

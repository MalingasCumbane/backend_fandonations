with open('src/main/java/com/donations/donations/service/admin/ReviewCreatorUseCase.java', 'r') as f:
    content = f.read()

import re

# Add imports
imports = """import com.donations.donations.repository.UserRepository;
import com.donations.donations.repository.EmailRepository;
"""
content = content.replace('import lombok.RequiredArgsConstructor;', imports + 'import lombok.RequiredArgsConstructor;')

# Add fields
fields = """    private final VerificationRequestRepository verificationRequestRepository;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;"""
content = content.replace('private final VerificationRequestRepository verificationRequestRepository;', fields)

# Send Email
notify_code = """        // Notify
        notificationRepository.save(Notification.builder()
                .id(UUID.randomUUID())
                .userId(creator.getUserId())
                .title("Atualização de Conta")
                .body("O estado da sua conta foi actualizado para " + status.name())
                .isRead(false)
                .build());

        // Send Email
        userRepository.findById(creator.getUserId()).ifPresent(u -> {
            emailRepository.sendAccountStatusEmail(u.getEmail(), status.name(), adminNote);
        });"""
content = content.replace("""        // Notify
        notificationRepository.save(Notification.builder()
                .id(UUID.randomUUID())
                .userId(creator.getUserId())
                .title("Atualização de Conta")
                .body("O estado da sua conta foi actualizado para " + status.name())
                .isRead(false)
                .build());""", notify_code)

with open('src/main/java/com/donations/donations/service/admin/ReviewCreatorUseCase.java', 'w') as f:
    f.write(content)

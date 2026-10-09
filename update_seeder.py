import re

file_path = "src/main/java/com/donations/donations/infrastructure/config/DatabaseSeeder.java"

with open(file_path, "r") as f:
    content = f.read()

imports = """import com.donations.donations.infrastructure.persistence.entity.UserJpaEntity;
import com.donations.donations.infrastructure.persistence.repository.UserJpaRepository;
import com.donations.donations.domain.model.UserRole;
import com.donations.donations.application.port.out.PasswordEncoderPort;
import com.donations.donations.infrastructure.persistence.entity.PlatformSettingsJpaEntity;
import com.donations.donations.infrastructure.persistence.repository.PlatformSettingsJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.util.UUID;
"""

new_content = content.replace("import java.util.List;", "import java.util.List;\n" + imports)

fields = """
    private final CategoryJpaRepository categoryRepository;
    private final CountryJpaRepository countryRepository;
    private final UserJpaRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final PlatformSettingsJpaRepository platformSettingsRepository;

    @Value("${app.admin.email:admin@donations.co.mz}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;
"""

new_content = re.sub(r'private final CategoryJpaRepository.*?\n.*?CountryJpaRepository.*?;', fields.strip(), new_content, flags=re.DOTALL)

seed_logic = """
        // Seed Platform Settings
        UUID settingsId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        if (!platformSettingsRepository.existsById(settingsId)) {
            PlatformSettingsJpaEntity settings = new PlatformSettingsJpaEntity();
            settings.setId(settingsId);
            settings.setPlatformFeePercentage(new BigDecimal("10.00"));
            settings.setMinPayoutAmount(new BigDecimal("500.00"));
            settings.setPayoutsEnabled(true);
            settings.setRegistrationsOpen(true);
            platformSettingsRepository.save(settings);
        }

        // Seed Admin User
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            UserJpaEntity admin = new UserJpaEntity();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(UserRole.SUPER_ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
        }
"""

new_content = new_content.replace("categoryRepository.deleteAll();", seed_logic + "\n        categoryRepository.deleteAll();")

with open(file_path, "w") as f:
    f.write(new_content)

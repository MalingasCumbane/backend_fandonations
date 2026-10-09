import os

base_pkg = "src/main/java/com/donations/donations"
usecase_dir = f"{base_pkg}/application/usecase/creator"
os.makedirs(usecase_dir, exist_ok=True)

# Generate placeholders for the creator use cases
usecases = {
    "UpdateCreatorSettingsUseCase.java": """package com.donations.donations.application.usecase.creator;
import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCreatorSettingsUseCase {
    private final CreatorPort creatorPort;
    public Creator execute(UUID userId, Object dto) {
        // Mock
        return creatorPort.findByUserId(userId).orElseThrow();
    }
}
""",
    "RequestFullNameChangeUseCase.java": """package com.donations.donations.application.usecase.creator;
import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestFullNameChangeUseCase {
    private final CreatorPort creatorPort;
    public Creator execute(UUID userId, String requestedFullName) {
        // Mock
        Creator creator = creatorPort.findByUserId(userId).orElseThrow();
        creator.setRequestedFullName(requestedFullName);
        return creatorPort.save(creator);
    }
}
"""
}

for name, content in usecases.items():
    with open(f"{usecase_dir}/{name}", "w") as f:
        f.write(content)

print("Generated creator usecases")

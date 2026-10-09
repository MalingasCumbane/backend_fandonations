import os
import re

base_pkg = "com.donations.donations"
base_dir = "/Volumes/MC/Malingas/Projs/Donations/donations/src/main/java/com/donations/donations"

entities = {
    "Campaign": {
        "fields": [
            ("UUID", "id"),
            ("UUID", "creatorId"),
            ("String", "title"),
            ("String", "description"),
            ("String", "imageUrl"),
            ("BigDecimal", "goalAmount"),
            ("BigDecimal", "raisedAmount"),
            ("LocalDate", "startDate"),
            ("LocalDate", "endDate"),
            ("CampaignStatus", "status"),
        ],
        "imports": ["java.math.BigDecimal", "java.time.LocalDate", "java.util.UUID", f"{base_pkg}.domain.model.enums.CampaignStatus"]
    },
    "Donation": {
        "fields": [
            ("UUID", "id"),
            ("UUID", "creatorId"),
            ("UUID", "campaignId"),
            ("String", "supporterName"),
            ("String", "supporterEmail"),
            ("String", "supporterPhone"),
            ("String", "message"),
            ("boolean", "anonymous"),
            ("BigDecimal", "amount"),
            ("BigDecimal", "feeAmount"),
            ("BigDecimal", "netAmount"),
            ("PaymentMethod", "method"),
            ("DonationStatus", "status"),
            ("String", "failureReason"),
        ],
        "imports": ["java.math.BigDecimal", "java.util.UUID", f"{base_pkg}.domain.model.enums.DonationStatus", f"{base_pkg}.domain.model.enums.PaymentMethod"]
    },
    "Payout": {
        "fields": [
            ("UUID", "id"),
            ("UUID", "creatorId"),
            ("BigDecimal", "amount"),
            ("PaymentMethod", "method"),
            ("String", "phone"),
            ("PayoutStatus", "status"),
            ("LocalDateTime", "requestedAt"),
            ("LocalDateTime", "processedAt"),
            ("String", "adminNote"),
            ("String", "transactionReference"),
        ],
        "imports": ["java.math.BigDecimal", "java.time.LocalDateTime", "java.util.UUID", f"{base_pkg}.domain.model.enums.PayoutStatus", f"{base_pkg}.domain.model.enums.PaymentMethod"]
    },
    "VerificationRequest": {
        "fields": [
            ("UUID", "id"),
            ("UUID", "creatorId"),
            ("String", "documentType"),
            ("String", "documentNumber"),
            ("String", "fullName"),
            ("String", "documentFile"),
            ("String", "documentFileType"),
            ("String", "selfieImage"),
            ("String", "note"),
            ("VerificationRequestStatus", "status"),
            ("LocalDateTime", "submittedAt"),
            ("LocalDateTime", "reviewedAt"),
            ("String", "adminNote"),
        ],
        "imports": ["java.time.LocalDateTime", "java.util.UUID", f"{base_pkg}.domain.model.enums.VerificationRequestStatus"]
    },
    "Report": {
        "fields": [
            ("UUID", "id"),
            ("UUID", "creatorId"),
            ("String", "reason"),
            ("String", "details"),
            ("String", "reporterEmail"),
            ("ReportStatus", "status"),
        ],
        "imports": ["java.util.UUID", f"{base_pkg}.domain.model.enums.ReportStatus"]
    },
    "Notification": {
        "fields": [
            ("UUID", "id"),
            ("UUID", "userId"),
            ("String", "title"),
            ("String", "body"),
            ("boolean", "isRead"),
        ],
        "imports": ["java.util.UUID"]
    },
    "PlatformSettings": {
        "fields": [
            ("UUID", "id"),
            ("BigDecimal", "platformFeePercentage"),
            ("BigDecimal", "minPayoutAmount"),
            ("boolean", "payoutsEnabled"),
            ("boolean", "registrationsOpen"),
        ],
        "imports": ["java.math.BigDecimal", "java.util.UUID"]
    },
    "Payment": {
        "fields": [
            ("UUID", "id"),
            ("UUID", "donationId"),
            ("String", "providerReference"),
            ("String", "thirdPartyReference"),
            ("String", "rawResponse"),
        ],
        "imports": ["java.util.UUID"]
    }
}

def create_dir(path):
    os.makedirs(path, exist_ok=True)

repo_dir = f"{base_dir}/infrastructure/persistence/repository"
domain_dir = f"{base_dir}/domain/model"
port_dir = f"{base_dir}/application/port/out"
adapter_dir = f"{base_dir}/infrastructure/persistence/adapter"
mapper_dir = f"{base_dir}/infrastructure/persistence/mapper"

for d in [repo_dir, domain_dir, port_dir, adapter_dir, mapper_dir]:
    create_dir(d)

for entity, config in entities.items():
    # 1. Repository
    repo_content = f"""package {base_pkg}.infrastructure.persistence.repository;

import {base_pkg}.infrastructure.persistence.entity.{entity}JpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface {entity}JpaRepository extends JpaRepository<{entity}JpaEntity, UUID> {{
}}
"""
    with open(f"{repo_dir}/{entity}JpaRepository.java", "w") as f:
        f.write(repo_content)

    # 2. Domain Model
    imports = "\n".join([f"import {imp};" for imp in config['imports']])
    fields = "\n".join([f"    private {typ} {name};" for typ, name in config['fields']])
    domain_content = f"""package {base_pkg}.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

{imports}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {entity} {{
{fields}
}}
"""
    with open(f"{domain_dir}/{entity}.java", "w") as f:
        f.write(domain_content)

    # 3. Port
    port_content = f"""package {base_pkg}.application.port.out;

import {base_pkg}.domain.model.{entity};
import java.util.Optional;
import java.util.UUID;

public interface {entity}Port {{
    {entity} save({entity} {entity.lower()});
    Optional<{entity}> findById(UUID id);
}}
"""
    with open(f"{port_dir}/{entity}Port.java", "w") as f:
        f.write(port_content)

    # 4. Mapper
    mapper_content = f"""package {base_pkg}.infrastructure.persistence.mapper;

import {base_pkg}.domain.model.{entity};
import {base_pkg}.infrastructure.persistence.entity.{entity}JpaEntity;
import org.springframework.stereotype.Component;

@Component
public class {entity}Mapper {{

    public static {entity} toDomain({entity}JpaEntity entity) {{
        if (entity == null) return null;
        return {entity}.builder()
"""
    for typ, name in config['fields']:
        mapper_content += f"            .{name}(entity.get{name[0].upper()}{name[1:]}())\n"
    mapper_content += f"""            .build();
    }}

    public static {entity}JpaEntity toEntity({entity} domain) {{
        if (domain == null) return null;
        return {entity}JpaEntity.builder()
"""
    for typ, name in config['fields']:
        # Fix for boolean getter convention if needed, standard lombok uses isX for boolean and getX for Boolean
        # Actually lombok for boolean generates isX, but let's assume getX works for boolean or we just use isX
        getter_prefix = "is" if typ == "boolean" else "get"
        mapper_content += f"            .{name}(domain.{getter_prefix}{name[0].upper()}{name[1:]}())\n"
    mapper_content += f"""            .build();
    }}
}}
"""
    with open(f"{mapper_dir}/{entity}Mapper.java", "w") as f:
        f.write(mapper_content)

    # 5. Adapter
    adapter_content = f"""package {base_pkg}.infrastructure.persistence.adapter;

import {base_pkg}.application.port.out.{entity}Port;
import {base_pkg}.domain.model.{entity};
import {base_pkg}.infrastructure.persistence.entity.{entity}JpaEntity;
import {base_pkg}.infrastructure.persistence.mapper.{entity}Mapper;
import {base_pkg}.infrastructure.persistence.repository.{entity}JpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class {entity}PersistenceAdapter implements {entity}Port {{

    private final {entity}JpaRepository {entity.lower()}Repository;

    @Override
    public {entity} save({entity} {entity.lower()}) {{
        {entity}JpaEntity entity = {entity}Mapper.toEntity({entity.lower()});
        {entity}JpaEntity savedEntity = {entity.lower()}Repository.save(entity);
        return {entity}Mapper.toDomain(savedEntity);
    }}

    @Override
    public Optional<{entity}> findById(UUID id) {{
        return {entity.lower()}Repository.findById(id)
                .map({entity}Mapper::toDomain);
    }}
}}
"""
    with open(f"{adapter_dir}/{entity}PersistenceAdapter.java", "w") as f:
        f.write(adapter_content)


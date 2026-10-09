def fix_file(filepath, class_name, repo_class, ret_type):
    content = f"""package com.donations.donations.service.admin;

import com.donations.donations.model.{ret_type};
import com.donations.donations.repository.{repo_class};
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class {class_name} {{
    private final {repo_class} repository;

    public List<{ret_type}> execute() {{
        return repository.findAll();
    }}
}}
"""
    with open(filepath, 'w') as f:
        f.write(content)

fix_file('src/main/java/com/donations/donations/service/admin/GetDonationsUseCase.java', 'GetDonationsUseCase', 'DonationRepository', 'Donation')
fix_file('src/main/java/com/donations/donations/service/admin/GetPayoutsUseCase.java', 'GetPayoutsUseCase', 'PayoutRepository', 'Payout')
fix_file('src/main/java/com/donations/donations/service/admin/GetReportsUseCase.java', 'GetReportsUseCase', 'ReportRepository', 'Report')

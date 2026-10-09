import re

with open('src/main/java/com/donations/donations/controller/PublicDataController.java', 'r') as f:
    content = f.read()

# Fix fields location
content = content.replace("    private final CreatorRepository creatorJpaRepository;\n\n    private final ProcessDonationUseCase processDonationUseCase;", "")

fields = """    private final CountryRepository countryRepository;
    private final CategoryRepository categoryRepository;
    private final CreatorRepository creatorJpaRepository;
    private final ProcessDonationUseCase processDonationUseCase;
    private final com.donations.donations.repository.CampaignRepository campaignRepository;
"""
content = content.replace("    private final CountryRepository countryRepository;\n    private final CategoryRepository categoryRepository;", fields)

# Update getCreator to include campaigns
old_map = """                    // Return empty campaigns and stats for now (or mock)
                    // The frontend just needs the structure to not be undefined
                    return PublicCreatorProfile.builder()
                            .creator(publicCreator)
                            .campaigns(java.util.List.of())
                            .stats(java.util.Map.of("supporterCount", 0, "donationCount", 0))
                            .build();"""

new_map = """                    List<Campaign> campaigns = campaignRepository.findByCreatorId(c.getId()).stream()
                            .filter(camp -> camp.getStatus() != null && camp.getStatus().name().equals("ACTIVE"))
                            .collect(Collectors.toList());

                    return PublicCreatorProfile.builder()
                            .creator(publicCreator)
                            .campaigns(campaigns)
                            .stats(java.util.Map.of("supporterCount", 0, "donationCount", 0))
                            .build();"""

content = content.replace(old_map, new_map)

with open('src/main/java/com/donations/donations/controller/PublicDataController.java', 'w') as f:
    f.write(content)

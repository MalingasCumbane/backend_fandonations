with open('src/main/java/com/donations/donations/service/creator/UpdateCampaignUseCase.java', 'r') as f:
    content = f.read()

replacement = """
    public Campaign execute(UUID campaignId, Campaign updated) {
        return campaignRepository.findById(campaignId)
            .map(existing -> {
                if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
                if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
                if (updated.getGoalAmount() != null) existing.setGoalAmount(updated.getGoalAmount());
                if (updated.getStartDate() != null) existing.setStartDate(updated.getStartDate());
                if (updated.getEndDate() != null) existing.setEndDate(updated.getEndDate());
                if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
                return campaignRepository.save(existing);
            })
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
    }
"""

# Find the execute method block
start_idx = content.find("public Campaign execute")
end_idx = content.find("}", content.find(".orElseThrow")) + 1

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + replacement.strip() + content[end_idx:]
    with open('src/main/java/com/donations/donations/service/creator/UpdateCampaignUseCase.java', 'w') as f:
        f.write(content)
        print("Updated successfully")
else:
    print("Could not find block")

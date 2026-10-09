import re

with open('src/main/java/com/donations/donations/controller/CreatorApiController.java', 'r') as f:
    content = f.read()

content = content.replace("updateCampaignUseCase.execute(id, campaign)", "updateCampaignUseCase.execute(id, campaign, getCreatorId(userDetails.getId()))")
content = content.replace("deleteCampaignUseCase.execute(id)", "deleteCampaignUseCase.execute(id, getCreatorId(userDetails.getId()))")

with open('src/main/java/com/donations/donations/controller/CreatorApiController.java', 'w') as f:
    f.write(content)


with open('src/main/java/com/donations/donations/service/creator/DeleteCampaignUseCase.java', 'r') as f:
    content = f.read()

old_delete = """    public void execute(UUID campaignId) {
        campaignRepository.deleteById(campaignId);
    }"""
new_delete = """    public void execute(UUID campaignId, UUID creatorId) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            if (campaign.getCreatorId().equals(creatorId)) {
                campaignRepository.deleteById(campaignId);
            } else {
                throw new org.springframework.security.access.AccessDeniedException("Não tem permissão para apagar esta campanha.");
            }
        });
    }"""
content = content.replace(old_delete, new_delete)
with open('src/main/java/com/donations/donations/service/creator/DeleteCampaignUseCase.java', 'w') as f:
    f.write(content)

with open('src/main/java/com/donations/donations/service/creator/UpdateCampaignUseCase.java', 'r') as f:
    content = f.read()

old_update = """    public Campaign execute(UUID campaignId, Campaign updated) {
        return campaignRepository.findById(campaignId)
            .map(existing -> {"""
new_update = """    public Campaign execute(UUID campaignId, Campaign updated, UUID creatorId) {
        return campaignRepository.findById(campaignId)
            .map(existing -> {
                if (!existing.getCreatorId().equals(creatorId)) {
                    throw new org.springframework.security.access.AccessDeniedException("Não tem permissão para editar esta campanha.");
                }"""
content = content.replace(old_update, new_update)
with open('src/main/java/com/donations/donations/service/creator/UpdateCampaignUseCase.java', 'w') as f:
    f.write(content)


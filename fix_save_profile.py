with open('src/main/java/com/donations/donations/service/SaveCreatorProfileUseCase.java', 'r') as f:
    content = f.read()

import re

# Insert the logic for VERIFIED creators
logic = """        if (existingOpt.isPresent()) {
            Creator existing = existingOpt.get();
            
            if (existing.getStatus() == CreatorStatus.VERIFIED) {
                // If verified, only allow updating socials, avatar and marketing preferences.
                existing.setFacebookUrl(input.getFacebookUrl() != null ? input.getFacebookUrl() : existing.getFacebookUrl());
                existing.setTwitterUrl(input.getTwitterUrl() != null ? input.getTwitterUrl() : existing.getTwitterUrl());
                existing.setInstagramUrl(input.getInstagramUrl() != null ? input.getInstagramUrl() : existing.getInstagramUrl());
                existing.setYoutubeUrl(input.getYoutubeUrl() != null ? input.getYoutubeUrl() : existing.getYoutubeUrl());
                existing.setTiktokUrl(input.getTiktokUrl() != null ? input.getTiktokUrl() : existing.getTiktokUrl());
                existing.setAcceptsMarketing(input.getAcceptsMarketing() != null ? input.getAcceptsMarketing() : existing.isAcceptsMarketing());
                existing.setAvatarUrl(input.getAvatarUrl() != null ? input.getAvatarUrl() : existing.getAvatarUrl());
                return creatorRepository.save(existing);
            }

            // Update fields"""

content = content.replace("""        if (existingOpt.isPresent()) {
            Creator existing = existingOpt.get();
            // Update fields""", logic)

with open('src/main/java/com/donations/donations/service/SaveCreatorProfileUseCase.java', 'w') as f:
    f.write(content)

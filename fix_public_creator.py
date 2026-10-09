import re

with open('src/main/java/com/donations/donations/controller/PublicDataController.java', 'r') as f:
    content = f.read()

# Replace getCreator return type and body
old_builder = """                    PublicCreator publicCreator = PublicCreator.builder()
                            .id(c.getId())
                            .username(c.getUsername())
                            .fullName(c.getFullName())
                            .category(c.getCategory() != null ? c.getCategory().getName() : null)
                            .bio(c.getBio())
                            .country(c.getCountry() != null ? c.getCountry().getName() : null)
                            .build();"""

new_builder = """                    PublicCreator publicCreator = PublicCreator.builder()
                            .id(c.getId())
                            .userId(c.getUserId())
                            .username(c.getUsername())
                            .fullName(c.getFullName())
                            .category(c.getCategory() != null ? c.getCategory().getName() : null)
                            .bio(c.getBio())
                            .country(c.getCountry() != null ? c.getCountry().getName() : null)
                            .avatarUrl(c.getAvatarUrl())
                            .status(c.getStatus())
                            .createdAt(c.getCreatedAt())
                            .socials(PublicCreator.Socials.builder()
                                    .facebook(c.getFacebookUrl())
                                    .twitter(c.getTwitterUrl())
                                    .instagram(c.getInstagramUrl())
                                    .youtube(c.getYoutubeUrl())
                                    .tiktok(c.getTiktokUrl())
                                    .build())
                            .build();"""

content = content.replace(old_builder, new_builder)

with open('src/main/java/com/donations/donations/controller/PublicDataController.java', 'w') as f:
    f.write(content)

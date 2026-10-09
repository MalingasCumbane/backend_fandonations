import re

# 1. Update Creator.java
with open('src/main/java/com/donations/donations/model/Creator.java', 'r') as f:
    creator_content = f.read()
if 'boolean acceptsMarketing' not in creator_content:
    creator_content = creator_content.replace('private String tiktokUrl;', 'private String tiktokUrl;\n\n    @Builder.Default\n    private boolean acceptsMarketing = false;')
    with open('src/main/java/com/donations/donations/model/Creator.java', 'w') as f:
        f.write(creator_content)

# 2. Update CreatorProfileRequestDto.java
with open('src/main/java/com/donations/donations/dto/creator/CreatorProfileRequestDto.java', 'r') as f:
    dto_content = f.read()
if 'Boolean acceptsMarketing' not in dto_content:
    dto_content = dto_content.replace('private String tiktokUrl;', 'private String tiktokUrl;\n    private Boolean acceptsMarketing;')
    with open('src/main/java/com/donations/donations/dto/creator/CreatorProfileRequestDto.java', 'w') as f:
        f.write(dto_content)

# 3. Update SaveCreatorProfileUseCase.java
with open('src/main/java/com/donations/donations/service/SaveCreatorProfileUseCase.java', 'r') as f:
    usecase_content = f.read()
if 'setAcceptsMarketing' not in usecase_content:
    # Existing update
    usecase_content = usecase_content.replace('existing.setTiktokUrl(input.getTiktokUrl() != null ? input.getTiktokUrl() : existing.getTiktokUrl());',
                                              'existing.setTiktokUrl(input.getTiktokUrl() != null ? input.getTiktokUrl() : existing.getTiktokUrl());\n            existing.setAcceptsMarketing(input.getAcceptsMarketing() != null ? input.getAcceptsMarketing() : existing.isAcceptsMarketing());')
    # New creator
    usecase_content = usecase_content.replace('.tiktokUrl(input.getTiktokUrl())',
                                              '.tiktokUrl(input.getTiktokUrl())\n                    .acceptsMarketing(input.getAcceptsMarketing() != null ? input.getAcceptsMarketing() : false)')
    with open('src/main/java/com/donations/donations/service/SaveCreatorProfileUseCase.java', 'w') as f:
        f.write(usecase_content)

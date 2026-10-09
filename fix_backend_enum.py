import re

# Update enum
with open('src/main/java/com/donations/donations/model/enums/VerificationRequestStatus.java', 'r') as f:
    content = f.read()
content = content.replace("PENDING_VERIFICATION", "PENDING_REQUEST")
with open('src/main/java/com/donations/donations/model/enums/VerificationRequestStatus.java', 'w') as f:
    f.write(content)

# Update SubmitVerificationUseCase
with open('src/main/java/com/donations/donations/service/creator/SubmitVerificationUseCase.java', 'r') as f:
    content = f.read()
content = content.replace("PENDING_VERIFICATION", "PENDING_REQUEST")
with open('src/main/java/com/donations/donations/service/creator/SubmitVerificationUseCase.java', 'w') as f:
    f.write(content)

# Update ReviewCreatorUseCase
with open('src/main/java/com/donations/donations/service/admin/ReviewCreatorUseCase.java', 'r') as f:
    content = f.read()
content = content.replace("VerificationRequestStatus.PENDING", "VerificationRequestStatus.PENDING_REQUEST")
with open('src/main/java/com/donations/donations/service/admin/ReviewCreatorUseCase.java', 'w') as f:
    f.write(content)

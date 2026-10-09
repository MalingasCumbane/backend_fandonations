import re

with open('src/main/java/com/donations/donations/service/VerifyEmailUseCase.java', 'r') as f:
    content = f.read()

content = content.replace("token.isExpired()", "token.getExpiresAt().isBefore(java.time.LocalDateTime.now())")
content = content.replace("user.activate();", "user.setEmailVerified(true);\n        user.setActive(true);")

with open('src/main/java/com/donations/donations/service/VerifyEmailUseCase.java', 'w') as f:
    f.write(content)

import re

with open('src/main/java/com/donations/donations/service/impl/AsyncPaymentProcessor.java', 'r') as f:
    content = f.read()

# Remove @Async
content = content.replace("@Async\n    @Transactional", "@Transactional")
content = content.replace("import org.springframework.scheduling.annotation.Async;\n", "")

with open('src/main/java/com/donations/donations/service/impl/AsyncPaymentProcessor.java', 'w') as f:
    f.write(content)

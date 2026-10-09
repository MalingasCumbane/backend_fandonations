import re

with open('src/main/java/com/donations/donations/service/external/RabbitMQEmailPublisherAdapter.java', 'r') as f:
    content = f.read()

# Wrap rabbitTemplate.convertAndSend in try/catch for all occurrences
def replace_convert(match):
    return """        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, event);
        } catch (Exception e) {
            System.err.println("Failed to send email to RabbitMQ: " + e.getMessage());
        }"""

content = re.sub(r'rabbitTemplate\.convertAndSend\(RabbitMQConfig\.EMAIL_EXCHANGE,\s*RabbitMQConfig\.EMAIL_ROUTING_KEY,\s*event\);', replace_convert, content)

with open('src/main/java/com/donations/donations/service/external/RabbitMQEmailPublisherAdapter.java', 'w') as f:
    f.write(content)

with open('src/main/java/com/donations/donations/service/impl/AsyncPaymentProcessor.java', 'r') as f:
    content = f.read()

old_call = """                    emailRepository.sendDonationReceiptEmail(
                        donation.getSupporterEmail(), 
                        creator.getFullName(), 
                        donation.getAmount().toString(), 
                        donation.getReference()
                    );"""

new_call = """                    emailRepository.sendDonationReceiptEmail(
                        donation.getSupporterEmail(), 
                        creator.getFullName(), 
                        donation.getAmount().toString(), 
                        donation.getReference(),
                        donation.getMethod() != null ? donation.getMethod().name() : "M-Pesa / e-Mola"
                    );"""

content = content.replace(old_call, new_call)
with open('src/main/java/com/donations/donations/service/impl/AsyncPaymentProcessor.java', 'w') as f:
    f.write(content)
print("Updated AsyncPaymentProcessor")

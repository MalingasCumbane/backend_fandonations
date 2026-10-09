with open('src/main/java/com/donations/donations/service/impl/AsyncPaymentProcessor.java', 'r') as f:
    content = f.read()

# Add EmailRepository
imports = "import com.donations.donations.repository.EmailRepository;\n"
content = content.replace("import com.donations.donations.model.enums.DonationStatus;", imports + "import com.donations.donations.model.enums.DonationStatus;")

fields = """    private final NotificationRepository notificationRepository;
    private final CreatorRepository creatorRepository;
    private final EmailRepository emailRepository;"""
content = content.replace("    private final NotificationRepository notificationRepository;\n    private final CreatorRepository creatorRepository;", fields)

# Send email
old_notification = """                notificationRepository.save(notification);
            }
        } else {"""
        
new_notification = """                notificationRepository.save(notification);
                
                if (donation.getSupporterEmail() != null && !donation.getSupporterEmail().isEmpty()) {
                    emailRepository.sendDonationReceiptEmail(
                        donation.getSupporterEmail(), 
                        creator.getFullName(), 
                        donation.getAmount().toString(), 
                        donation.getReference()
                    );
                }
            }
        } else {"""
        
content = content.replace(old_notification, new_notification)

with open('src/main/java/com/donations/donations/service/impl/AsyncPaymentProcessor.java', 'w') as f:
    f.write(content)

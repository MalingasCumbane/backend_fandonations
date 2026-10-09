import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'r') as f:
    content = f.read()

# 1. Update handleDonate to show a toast message
old_donate = """      const donation = await donationService.donate(creator, {
        campaignId,
        amount: finalAmount,
        method,
        phone: supporterPhone,
        message,
        anonymous,
        supporterName: anonymous ? undefined : supporterName,
        supporterEmail: anonymous ? undefined : supporterEmail,
      });
      setReceipt(donation);
      onDone();
    } catch (error) {"""

new_donate = """      const donation = await donationService.donate(creator, {
        campaignId,
        amount: finalAmount,
        method,
        phone: supporterPhone,
        message,
        anonymous,
        supporterName: anonymous ? undefined : supporterName,
        supporterEmail: anonymous ? undefined : supporterEmail,
      });
      
      if (donation.status === "FAILED") {
        toast.error(donation.failureReason || "O pagamento falhou.");
        // We can still show the receipt to let them know it failed, or not.
        // Actually showing the receipt with FAILED status is better.
      } else {
        toast.success("Pagamento confirmado com sucesso!");
      }
      
      setReceipt(donation);
      onDone();
    } catch (error) {"""
content = content.replace(old_donate, new_donate)

# 2. Update Receipt to display the status properly
old_receipt_state = """          ["Estado", donation.status === "SUCCESS" ? "Pagamento confirmado" : "Em processamento"],"""
new_receipt_state = """          ["Estado", donation.status === "SUCCESS" ? "✅ Confirmado" : donation.status === "FAILED" ? "❌ Falhou" : "⏳ Em processamento"],"""
content = content.replace(old_receipt_state, new_receipt_state)

# If it failed, also add a row for Failure Reason
old_map = """        ].map(([label, value]) => (
          <div"""
new_map = """        ].filter(Boolean).map(([label, value]) => (
          <div"""
content = content.replace(old_map, new_map)

old_array_end = """          ["Estado", donation.status === "SUCCESS" ? "✅ Confirmado" : donation.status === "FAILED" ? "❌ Falhou" : "⏳ Em processamento"],
        ].filter(Boolean)"""

new_array_end = """          ["Estado", donation.status === "SUCCESS" ? "✅ Confirmado" : donation.status === "FAILED" ? "❌ Falhou" : "⏳ Em processamento"],
          donation.status === "FAILED" && donation.failureReason ? ["Motivo", donation.failureReason] : null,
        ].filter(Boolean) as [string, string][]"""
content = content.replace(old_array_end, new_array_end)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'w') as f:
    f.write(content)

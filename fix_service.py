with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/services/verification.service.ts', 'r') as f:
    content = f.read()

new_method = """  async submit(input: VerificationInput): Promise<VerificationRequest> {
    const response = await api.post<VerificationRequest>("/verifications/me", {
      ...input,
      note: input.note?.trim() || undefined,
    });
    return response.data;
  },

  async cancel(): Promise<void> {
    await api.delete("/verifications/me");
  },"""

import re
content = re.sub(r'async submit\(input: VerificationInput\): Promise<VerificationRequest> \{.*?return response.data;\n  \},', new_method, content, flags=re.DOTALL)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/services/verification.service.ts', 'w') as f:
    f.write(content)

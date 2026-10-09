with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/types/index.ts', 'r') as f:
    content = f.read()

if "export type VerificationRequestStatus =" not in content:
    content = content.replace("export interface VerificationRequest {", "export type VerificationRequestStatus = \"PENDING_REQUEST\" | \"APPROVED\" | \"REJECTED\";\n\nexport interface VerificationRequest {")

content = content.replace("status: CreatorStatus;\n  submittedAt: string;", "status: VerificationRequestStatus;\n  submittedAt: string;")

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/types/index.ts', 'w') as f:
    f.write(content)

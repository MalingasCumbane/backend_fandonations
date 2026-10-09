with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.verificacao.lazy.tsx', 'r') as f:
    content = f.read()

content = content.replace('current.status === "PENDING_VERIFICATION"', 'current.status === "PENDING_REQUEST" || current.status === "PENDING_VERIFICATION"')

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.verificacao.lazy.tsx', 'w') as f:
    f.write(content)

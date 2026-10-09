# Fix 1: admin.verificacoes.lazy.tsx
with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/admin.verificacoes.lazy.tsx', 'r') as f:
    content = f.read()

content = content.replace('req.status === "VERIFIED"', 'req.status === "APPROVED"')
# Wait, handleReview(req.creatorId, "VERIFIED") uses CreatorStatus! But we use req.status === "APPROVED" for rendering the badge.
with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/admin.verificacoes.lazy.tsx', 'w') as f:
    f.write(content)

# Fix 2: dashboard.verificacao.lazy.tsx
with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.verificacao.lazy.tsx', 'r') as f:
    content = f.read()

content = content.replace('current.status === "PENDING_REQUEST" || current.status === "PENDING_VERIFICATION"', 'current.status === "PENDING_VERIFICATION"')
with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.verificacao.lazy.tsx', 'w') as f:
    f.write(content)

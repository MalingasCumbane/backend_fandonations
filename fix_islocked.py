with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.perfil.lazy.tsx', 'r') as f:
    content = f.read()

content = content.replace("function CompleteProfilePage() {\n  const { creator, refresh } = useAuth();", "function CompleteProfilePage() {\n  const { creator, refresh } = useAuth();\n  const isLocked = creator?.status === \"VERIFIED\";")

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.perfil.lazy.tsx', 'w') as f:
    f.write(content)

import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.perfil.lazy.tsx', 'r') as f:
    content = f.read()

# 1. Update option values to use IDs instead of names
content = content.replace('<option key={c.id} value={c.name}>', '<option key={c.id} value={c.id}>')
content = content.replace('<option key={cat.id} value={cat.name}>', '<option key={cat.id} value={cat.id}>')

# 2. Disable all fields if verified
# Add `const isLocked = creator?.status === "VERIFIED";`
content = content.replace('const current = creator;', 'const current = creator;\n  const isLocked = creator?.status === "VERIFIED";')

# Inject `disabled={isLocked}` into the inputs/selects/textareas.
# Wait, some inputs might already have disabled={something}.
# I'll replace `className={field}` with `disabled={isLocked} className={field}` where possible.
# Actually, the username input already has logic:
# `disabled={usernameStatus === "checking"}` or `disabled={true}` from before? Let's check username input.

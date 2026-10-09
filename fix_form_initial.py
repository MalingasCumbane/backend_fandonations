with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.perfil.lazy.tsx', 'r') as f:
    content = f.read()

# Replace country and category initializations
old_country = "country: creator?.country ?? \"\","
new_country = "country: (typeof creator?.country === 'object' ? (creator?.country as any)?.id : creator?.country) ?? \"\","

old_category = "category: creator?.category ?? \"\","
new_category = "category: (typeof creator?.category === 'object' ? (creator?.category as any)?.id : creator?.category) ?? \"\","

content = content.replace(old_country, new_country)
content = content.replace(old_category, new_category)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.perfil.lazy.tsx', 'w') as f:
    f.write(content)

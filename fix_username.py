import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'r') as f:
    content = f.read()

# Remove the `@` requirement from loader
# Also replace `const handle = params.username.replace(/^@/, "");` with just `const handle = params.username;`

content = re.sub(r'loader: \(\{\s*params\s*\}\) => \{\s*if \(\!params\.username\.startsWith\("@"\)\) throw notFound\(\);\s*\},', '', content)

content = content.replace('const handle = params.username.replace(/^@/, "");', 'const handle = params.username;')
content = content.replace('const handle = username.replace(/^@/, "");', 'const handle = username;')

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'w') as f:
    f.write(content)

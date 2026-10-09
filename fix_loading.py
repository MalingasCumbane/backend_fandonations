import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'r') as f:
    content = f.read()

# Replace { data, refresh } with { data, loading, refresh }
content = content.replace("const { data, refresh } = useLocalData", "const { data, loading, refresh } = useLocalData")

# Replace {!data ? ( with {loading ? (
content = content.replace("{!data ? (", "{loading ? (")

# Then if not loading, check if we have data and data.creator
content = content.replace(") : !data.creator ? (", ") : (!data || !data.creator) ? (")

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'w') as f:
    f.write(content)

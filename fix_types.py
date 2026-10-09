with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/types/index.ts', 'r') as f:
    content = f.read()

# Revert broken social part
content = content.replace("  };\n  acceptsMarketing?: boolean;", "")
content = content.replace("tiktok?: string;\n\n  youtube?: string;", "tiktok?: string;\n  youtube?: string;")

# Add acceptsMarketing to Creator properly
content = content.replace("  socials: CreatorSocials;", "  socials: CreatorSocials;\n  acceptsMarketing?: boolean;")

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/types/index.ts', 'w') as f:
    f.write(content)

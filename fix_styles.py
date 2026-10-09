import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/styles.css', 'r') as f:
    content = f.read()

# Make background slightly darker (less blinding white)
content = content.replace("--background: oklch(0.982 0.006 82.4);", "--background: oklch(0.955 0.01 82.4); /* Softer, darker background */")

# Soften pure white cards
content = content.replace("--card: oklch(1 0 0);", "--card: oklch(0.99 0.003 82.4);")

# Increase border opacity for better contrast/highlight
content = content.replace("--border: oklch(0.276 0.02 163.1 / 10%);", "--border: oklch(0.276 0.02 163.1 / 16%);")
content = content.replace("--input: oklch(0.276 0.02 163.1 / 12%);", "--input: oklch(0.276 0.02 163.1 / 18%);")

# Also let's update glass utility to have slightly more card color to contrast with the new darker background
content = content.replace("background-color: color-mix(in oklab, var(--color-card) 60%, transparent);", "background-color: color-mix(in oklab, var(--color-card) 75%, transparent);")

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/styles.css', 'w') as f:
    f.write(content)

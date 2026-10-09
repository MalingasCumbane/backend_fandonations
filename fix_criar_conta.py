import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/criar-conta.lazy.tsx', 'r') as f:
    content = f.read()

# Add isLoading
if 'isLoading' not in content:
    content = content.replace("const { register } = useAuth();", "const { register } = useAuth();\n  const [isLoading, setIsLoading] = useState(false);")
    
# Replace try
content = content.replace("try {\n      await register", "setIsLoading(true);\n    try {\n      await register")

# Replace catch
content = re.sub(r'(catch \([^)]+\) \{[^\}]+\})', r'\1 finally { setIsLoading(false); }', content)

# Replace button
content = content.replace("<button className=\"w-full rounded-lg bg-primary py-3 text-sm font-medium text-primary-foreground transition-transform hover:-translate-y-0.5\">\n              Criar conta\n            </button>", "<button disabled={isLoading} className=\"w-full rounded-lg bg-primary py-3 text-sm font-medium text-primary-foreground transition-transform hover:-translate-y-0.5 disabled:opacity-50\">\n              {isLoading ? <div className=\"h-5 w-5 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto\" /> : \"Criar conta\"}\n            </button>")

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/criar-conta.lazy.tsx', 'w') as f:
    f.write(content)

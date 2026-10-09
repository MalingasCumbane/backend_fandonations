import re
import sys

def modify_file(filepath, btn_text):
    with open(filepath, 'r') as f:
        content = f.read()

    # Add isLoading
    if 'isLoading' not in content:
        content = re.sub(r'(const \w+ = useNavigate\(\);|const \{ \w+ \} = useAuth\(\);)',
                         r'\1\n  const [isLoading, setIsLoading] = useState(false);', 
                         content, count=1)
                         
    # Wrap submit body in try/finally
    content = content.replace("try {\n      const user", "setIsLoading(true);\n    try {\n      const user")
    content = content.replace("try {\n      await register", "setIsLoading(true);\n    try {\n      await register")
    content = content.replace("try {\n      await creatorService", "setIsLoading(true);\n    try {\n      await creatorService")
    
    content = re.sub(r'(catch \([^)]+\) \{[^\}]+\})', r'\1 finally { setIsLoading(false); }', content)
    
    # Replace button
    # Be very precise!
    btn_regex = f'<button([^>]*)>\\s*{btn_text}\\s*</button>'
    replacement = f'<button\\1 disabled={{isLoading}}>{{isLoading ? <div className="h-5 w-5 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto" /> : "{btn_text}"}}</button>'
    
    content = re.sub(btn_regex, replacement, content)

    with open(filepath, 'w') as f:
        f.write(content)

base = '/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/'
try:
    modify_file(base + 'entrar.lazy.tsx', 'Entrar')
    modify_file(base + 'criar-conta.lazy.tsx', 'Criar conta')
    
    # Perfil is more complex (has 3 buttons: send otp, verify otp, submit)
    # Let's just modify the main submit button for now
    modify_file(base + 'dashboard.perfil.lazy.tsx', 'Guardar e continuar')
except Exception as e:
    print(f"Error: {e}")

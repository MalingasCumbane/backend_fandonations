import os
import re

routes_dir = '/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes'

for filename in os.listdir(routes_dir):
    if not filename.endswith('.tsx'):
        continue
        
    filepath = os.path.join(routes_dir, filename)
    with open(filepath, 'r') as f:
        content = f.read()

    # We need to find the form submission functions. Usually 'async function submit' or 'async function save'
    # and add isLoading state if a form exists
    
    if '<form' in content and 'useState' in content:
        # Check if isLoading already exists
        if 'isLoading' not in content:
            # inject const [isLoading, setIsLoading] = useState(false);
            content = re.sub(r'(const \w+ = useNavigate\(\);|const \[form, setForm\] = useState|const \{ \w+ \} = useAuth\(\);)',
                             r'const [isLoading, setIsLoading] = useState(false);\n  \1', 
                             content, count=1)
                             
        # wrap the try/catch inside submit functions
        func_match = re.search(r'async function (\w+)\(event: React\.FormEvent\)([^\{]*)\{([^}]*try\s*\{)', content)
        if func_match:
            func_name = func_match.group(1)
            # Find the body of the function and inject setIsLoading(true)
            # This is tricky with regex. Let's do it manually with python
            lines = content.split('\n')
            inside_func = False
            for i, line in enumerate(lines):
                if f'async function {func_name}' in line:
                    inside_func = True
                if inside_func and 'try {' in line:
                    lines.insert(i, '    setIsLoading(true);')
                    break
                    
            # Find the corresponding catch and add finally
            content = '\n'.join(lines)
            
            # Add finally block after catch
            catch_block_regex = r'(catch \([^)]+\) \{[^}]+\})'
            content = re.sub(catch_block_regex, r'\1 finally { setIsLoading(false); }', content)
            
            # replace button
            content = re.sub(r'(<button[^>]*?)>', r'\1 disabled={isLoading}>', content)
            
            # inject spinner inside button text
            # This is dangerous because some buttons might have different text. Let's just find the first button inside the form.
            # actually we can just look for `>Criar conta<`, `>Entrar<`, `>Guardar<`, `>Pedir<`, `>Enviar<`
            
            replacements = {
                ">Criar conta<": ">{isLoading ? <div className=\"h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto\" /> : \"Criar conta\"}<",
                ">Entrar<": ">{isLoading ? <div className=\"h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto\" /> : \"Entrar\"}<",
                ">Guardar alterações<": ">{isLoading ? <div className=\"h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto\" /> : \"Guardar alterações\"}<",
                ">Pedir levantamento<": ">{isLoading ? <div className=\"h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto\" /> : \"Pedir levantamento\"}<",
                ">Enviar documentos<": ">{isLoading ? <div className=\"h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto\" /> : \"Enviar documentos\"}<",
                ">Guardar<": ">{isLoading ? <div className=\"h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto\" /> : \"Guardar\"}<",
                ">Criar campanha<": ">{isLoading ? <div className=\"h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto\" /> : \"Criar campanha\"}<"
            }
            
            for k, v in replacements.items():
                content = content.replace(k, v)

            with open(filepath, 'w') as f:
                f.write(content)


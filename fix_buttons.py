import os
import re

routes_dir = '/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes'

for filename in os.listdir(routes_dir):
    if not filename.endswith('.tsx'):
        continue
        
    filepath = os.path.join(routes_dir, filename)
    with open(filepath, 'r') as f:
        content = f.read()

    # Revert H1 mistakes if any
    content = re.sub(r'<h1([^>]*)>\{isLoading \? <div[^>]+> </div> : "([^"]+)"\}</h1>', r'<h1\1>\2</h1>', content)
    content = re.sub(r'<h1([^>]*)>\{isLoading \? <div[^>]+></div> : "([^"]+)"\}</h1>', r'<h1\1>\2</h1>', content)
    
    # Let's write a robust regex for <button ...>Text</button>
    # We want to replace Text inside a button with {isLoading ? spinner : "Text"}
    
    def button_repl(match):
        attrs = match.group(1)
        text = match.group(2).strip()
        # if already has spinner, don't touch
        if '{isLoading' in text:
            return match.group(0)
            
        return f'<button{attrs}>\n              {{isLoading ? <div className="h-5 w-5 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto" /> : "{text}"}}\n            </button>'

    content = re.sub(r'<button([^>]*)>\s*([^{<]+)\s*</button>', button_repl, content)

    with open(filepath, 'w') as f:
        f.write(content)


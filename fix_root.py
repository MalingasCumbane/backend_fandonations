import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/__root.tsx', 'r') as f:
    content = f.read()

import_statement = "import { GlobalLoader } from \"@/components/GlobalLoader\";\n"
content = re.sub(r'(import { AuthProvider } from "@/stores/auth-store";)', r'\1\n' + import_statement, content)

outlet_replacement = """        <Outlet />
        <Toaster position="top-center" />
        <GlobalLoader />"""
content = content.replace("        <Outlet />\n        <Toaster position=\"top-center\" />", outlet_replacement)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/__root.tsx', 'w') as f:
    f.write(content)

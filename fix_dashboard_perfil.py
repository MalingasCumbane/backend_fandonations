with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.perfil.lazy.tsx', 'r') as f:
    content = f.read()

import re

# 1. Add isLocked
content = content.replace("const current = creator;", "const current = creator;\n  const isLocked = creator?.status === \"VERIFIED\";")

# 2. Add the banner with the link
banner = """      <div className="sm:col-span-2">
        <p className="text-sm font-semibold">Complete o seu perfil</p>
        <p className="mt-1 text-sm text-muted-foreground">
          Os campos com * são obrigatórios para prosseguir.
        </p>
        {isLocked && (
          <div className="mt-4 rounded-lg border border-green-500/20 bg-green-500/10 p-4 text-green-700">
            <p className="text-sm font-medium">O seu perfil está verificado e bloqueado para alterações.</p>
            <p className="mt-1 text-xs">Partilhe o seu link: <a href={`/${creator?.username}`} target="_blank" rel="noreferrer" className="font-bold underline">{window.location.origin}/{creator?.username}</a></p>
          </div>
        )}
      </div>"""

content = re.sub(r'<div className="sm:col-span-2">\s*<p className="text-sm font-semibold">Complete o seu perfil</p>\s*<p className="mt-1 text-sm text-muted-foreground">\s*Os campos com \* são obrigatórios para prosseguir.\s*</p>\s*</div>', banner, content, flags=re.DOTALL)

# 3. Add disabled={isLocked} to fields
content = content.replace('value={form.fullName}', 'disabled={isLocked}\n          value={form.fullName}')

# For username, combine disabled
content = content.replace('required\n            value={form.username}', 'required\n            disabled={isLocked || usernameStatus === "checking"}\n            value={form.username}')

content = content.replace('<select\n          required\n          value={form.country}', '<select\n          required\n          disabled={isLocked}\n          value={form.country}')

content = content.replace('value={form.city}', 'disabled={isLocked}\n          value={form.city}')

content = content.replace('<select\n          required\n          value={form.category}', '<select\n          required\n          disabled={isLocked}\n          value={form.category}')

content = content.replace('maxLength={500}', 'maxLength={500}\n          disabled={isLocked}')

# Fix the option values for country/category
content = content.replace('<option key={c.id} value={c.name}>', '<option key={c.id} value={c.id}>')
content = content.replace('<option key={cat.id} value={cat.name}>', '<option key={cat.id} value={cat.id}>')

# Hide the submit button if isLocked
btn = """<button
        disabled={usernameStatus === "unavailable" || usernameStatus === "checking" || isLoading}
        className="rounded-lg bg-primary py-3 text-sm font-medium text-primary-foreground sm:col-span-2 disabled:opacity-50"
      >
        {isLoading ? <div className="h-5 w-5 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto" /> : "Guardar e continuar"}
      </button>"""

new_btn = """{!isLocked && (
      <button
        disabled={usernameStatus === "unavailable" || usernameStatus === "checking" || isLoading}
        className="rounded-lg bg-primary py-3 text-sm font-medium text-primary-foreground sm:col-span-2 disabled:opacity-50"
      >
        {isLoading ? <div className="h-5 w-5 animate-spin rounded-full border-2 border-current border-t-transparent mx-auto" /> : "Guardar e continuar"}
      </button>
      )}"""

content = content.replace(btn, new_btn)

# Also block the phone update buttons if isLocked
content = content.replace('onClick={handleChangePhone}', 'onClick={handleChangePhone} disabled={isLocked}')
content = content.replace('onClick={handleSendOtp}', 'onClick={handleSendOtp} disabled={isLocked || isSendingOtp || !form.phone}')
content = content.replace('disabled={isSendingOtp || !form.phone}', '') # Since we put it above

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.perfil.lazy.tsx', 'w') as f:
    f.write(content)

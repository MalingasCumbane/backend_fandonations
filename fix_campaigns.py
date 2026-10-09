import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.campanhas.lazy.tsx', 'r') as f:
    content = f.read()

# Filter out CANCELLED
old_empty = """      {data.length === 0 ? ("""
new_empty = """      {data.filter(c => c.status !== "CANCELLED").length === 0 ? ("""
content = content.replace(old_empty, new_empty)

old_map = """        <div className="grid gap-3 sm:grid-cols-2">
          {data.map((campaign) => {"""
new_map = """        <div className="grid gap-3 sm:grid-cols-2">
          {data.filter(c => c.status !== "CANCELLED").map((campaign) => {"""
content = content.replace(old_map, new_map)

# Add Cancelar button
old_buttons = """                  <button
                    onClick={() => startEdit(campaign)}
                    className="glass rounded-lg px-3 py-1.5 text-xs font-medium"
                  >
                    Editar
                  </button>
                  <button"""
new_buttons = """                  <button
                    onClick={() => startEdit(campaign)}
                    className="glass rounded-lg px-3 py-1.5 text-xs font-medium"
                  >
                    Editar
                  </button>
                  <button
                    onClick={async () => {
                      if (confirm("Tem a certeza que deseja cancelar esta campanha? Ela deixará de estar visível no seu painel.")) {
                        try {
                          await campaignService.update(campaign.id, { status: "CANCELLED" });
                          toast.success("Campanha cancelada com sucesso.");
                          refresh();
                        } catch (error) {
                          toast.error(error instanceof Error ? error.message : "Não foi possível cancelar.");
                        }
                      }
                    }}
                    className="glass rounded-lg px-3 py-1.5 text-xs font-medium text-amber-600"
                  >
                    Cancelar
                  </button>
                  <button"""
content = content.replace(old_buttons, new_buttons)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.campanhas.lazy.tsx', 'w') as f:
    f.write(content)

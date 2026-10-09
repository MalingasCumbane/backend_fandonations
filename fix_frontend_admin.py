with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/admin.verificacoes.lazy.tsx', 'r') as f:
    content = f.read()

# The frontend checks for "PENDING_VERIFICATION"
content = content.replace('req.status === "PENDING_VERIFICATION"', 'req.status === "PENDING_REQUEST"')

# Let's also expand the admin view to show the selfie and document since the user said "O admin deve: Poder ver todos os detalhes dessa conta... através da FK".
# Currently it just shows table rows. I'll add a details section or just the links.
replacement = """
                <td className="px-4 py-3 text-right">
                  {req.status === "PENDING_REQUEST" && (
                    <div className="flex justify-end gap-2">
                      <Button size="sm" onClick={() => handleReview(req.creatorId, "VERIFIED")}>
                        Aprovar
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleReview(req.creatorId, "REJECTED")}
                      >
                        Rejeitar
                      </Button>
                    </div>
                  )}
                  <div className="mt-2 text-xs text-muted-foreground flex justify-end gap-2">
                    {req.documentFile && <a href={req.documentFile} target="_blank" rel="noreferrer" className="underline">Ver Doc</a>}
                    {req.selfieImage && <a href={req.selfieImage} target="_blank" rel="noreferrer" className="underline">Ver Selfie</a>}
                  </div>
                </td>
"""
import re
content = re.sub(r'<td className="px-4 py-3 text-right">.*?</td>', replacement.strip(), content, flags=re.DOTALL)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/admin.verificacoes.lazy.tsx', 'w') as f:
    f.write(content)

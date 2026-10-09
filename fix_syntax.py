import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'r') as f:
    content = f.read()

# Let's fix the dl block.
# We will just replace the exact block.
dl_regex = r'<dl className="mx-auto mt-6 max-w-sm space-y-2 text-left text-sm">.*?</dl>'

new_dl = """<dl className="mx-auto mt-6 max-w-sm space-y-2 text-left text-sm">
        {(
          [
            ["Método", methodLabels[donation.method]],
            ["Referência", donation.reference],
            ["Data", formatDate(new Date().toISOString())],
            ["Estado", donation.status === "SUCCESS" ? "✅ Confirmado" : donation.status === "FAILED" ? "❌ Falhou" : "⏳ Em processamento"],
            donation.status === "FAILED" && donation.failureReason ? ["Motivo", donation.failureReason] : null,
          ].filter(Boolean) as [string, string][]
        ).map(([label, value]) => (
          <div
            key={label}
            className="bg-cream/70 hairline flex justify-between rounded-lg px-3 py-2"
          >
            <dt className="text-muted-foreground">{label}</dt>
            <dd className="font-medium">{value}</dd>
          </div>
        ))}
      </dl>"""

content = re.sub(dl_regex, new_dl, content, flags=re.DOTALL)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'w') as f:
    f.write(content)

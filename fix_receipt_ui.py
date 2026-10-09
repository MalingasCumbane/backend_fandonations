import re

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'r') as f:
    content = f.read()

# 1. Add lucide-react imports
if 'lucide-react' not in content:
    content = content.replace('import { useState } from "react";', 'import { useState } from "react";\nimport { CheckCircle2, XCircle, Clock } from "lucide-react";')

# 2. Replace Receipt function body
receipt_pattern = r'function Receipt\(\{[\s\S]*?\}\) \{[\s\S]*?async function share\(\) \{[\s\S]*?toast\.success\("Link copiado\."\);\n  \}[\s\S]*?return \([\s\S]*?\);\n\}'

new_receipt = """function Receipt({
  donation,
  creator,
  onBack,
}: {
  donation: PublicDonationStatus;
  creator: PublicCreator;
  onBack: () => void;
}) {
  async function share() {
    const url = `${window.location.origin}/@${creator.username}`;
    if (navigator.share) {
      await navigator.share({ title: `Apoiei ${creator.fullName} no APOIA`, url });
      return;
    }
    await navigator.clipboard.writeText(url);
    toast.success("Link copiado.");
  }

  const isSuccess = donation.status === "SUCCESS";
  const isFailed = donation.status === "FAILED";

  return (
    <div className="glass rounded-[1.4rem] p-8 text-center">
      <div className="mb-6 flex justify-center">
        {isSuccess ? (
          <div className="rounded-full bg-green-500/10 p-5">
            <CheckCircle2 className="size-16 text-green-500" strokeWidth={2.5} />
          </div>
        ) : isFailed ? (
          <div className="rounded-full bg-red-500/10 p-5">
            <XCircle className="size-16 text-red-500" strokeWidth={2.5} />
          </div>
        ) : (
          <div className="rounded-full bg-orange-500/10 p-5">
            <Clock className="size-16 text-orange-500" strokeWidth={2.5} />
          </div>
        )}
      </div>

      <h1 className="text-2xl font-semibold">
        {isSuccess ? "Pagamento Confirmado!" : isFailed ? "Pagamento Falhou" : "A Processar..."}
      </h1>
      
      {isSuccess && (
        <p className="mt-2 text-sm text-muted-foreground">
          Obrigado pelo seu apoio! Você acabou de doar para {creator.fullName}.
        </p>
      )}

      <p className="mt-6 text-5xl font-bold">{formatMT(donation.amount)}</p>

      <div className="mx-auto mt-8 max-w-sm overflow-hidden rounded-xl bg-cream/70 hairline">
        <div className="border-b border-black/5 bg-black/5 px-4 py-2 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
          Detalhes da Transação
        </div>
        <dl className="divide-y divide-black/5 text-left text-sm">
          {(
            [
              ["Criador", creator.fullName],
              ["Username", `@${creator.username}`],
              ["Método", methodLabels[donation.method]],
              ["Referência", donation.reference],
              ["Data", formatDate(new Date().toISOString())],
              isFailed && donation.failureReason ? ["Motivo", donation.failureReason] : null,
            ].filter(Boolean) as [string, string][]
          ).map(([label, value]) => (
            <div key={label} className="flex justify-between px-4 py-3">
              <dt className="text-muted-foreground">{label}</dt>
              <dd className="font-medium text-right">{value}</dd>
            </div>
          ))}
        </dl>
      </div>

      <div className="mt-8 flex flex-wrap justify-center gap-3">
        <button
          onClick={share}
          className="rounded-lg bg-primary px-5 py-3 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
        >
          Partilhar link do criador
        </button>
        <button
          onClick={async () => {
            await navigator.clipboard.writeText(donation.reference);
            toast.success("Referência copiada.");
          }}
          className="glass rounded-lg px-5 py-3 text-sm font-medium transition-colors hover:bg-cream"
        >
          Copiar Referência
        </button>
      </div>
      
      <button
        onClick={onBack}
        className="mt-6 text-sm font-medium text-muted-foreground hover:text-foreground underline underline-offset-4"
      >
        Voltar à página principal
      </button>
    </div>
  );
}"""

content = re.sub(receipt_pattern, new_receipt, content, flags=re.DOTALL)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/$username.tsx', 'w') as f:
    f.write(content)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.verificacao.lazy.tsx', 'r') as f:
    content = f.read()

# add import
if 'getApiErrorMessage' not in content:
    content = content.replace('import { SelfieCamera } from "@/components/apoia/selfie-camera";', 'import { SelfieCamera } from "@/components/apoia/selfie-camera";\nimport { getApiErrorMessage } from "@/lib/api-error";')

# add cancel button block
old_pending = """      ) : current.status === "PENDING_VERIFICATION" || current.status === "UNDER_REVIEW" ? (
        <div className="glass rounded-2xl p-6 lg:col-span-2">
          <h2 className="text-xl font-semibold">Informação submetida</h2>
          <p className="mt-2 text-sm text-muted-foreground">
            Os seus dados foram submetidos com sucesso. Por favor, aguarde pela nossa comunicação ou
            aprovação dos documentos.
          </p>
        </div>"""

new_pending = """      ) : current.status === "PENDING_VERIFICATION" || current.status === "UNDER_REVIEW" ? (
        <div className="glass rounded-2xl p-6 lg:col-span-2">
          <h2 className="text-xl font-semibold">Informação submetida</h2>
          <p className="mt-2 text-sm text-muted-foreground">
            Os seus dados foram submetidos com sucesso. Por favor, aguarde pela nossa comunicação ou
            aprovação dos documentos.
          </p>
          <div className="mt-6 flex justify-start">
            <button
              onClick={async () => {
                if (!confirm("Tem a certeza que deseja cancelar a submissão atual? Terá de enviar novos documentos.")) return;
                try {
                  setIsLoading(true);
                  await verificationService.cancel();
                  toast.success("Submissão cancelada com sucesso.");
                  refresh();
                  await refreshAuth();
                } catch (error) {
                  toast.error(getApiErrorMessage(error));
                } finally {
                  setIsLoading(false);
                }
              }}
              disabled={isLoading}
              className="rounded-lg border border-destructive bg-destructive/10 px-4 py-2 text-sm font-medium text-destructive transition-colors hover:bg-destructive hover:text-destructive-foreground disabled:opacity-50"
            >
              {isLoading ? "A cancelar..." : "Cancelar submissão"}
            </button>
          </div>
        </div>"""

content = content.replace(old_pending, new_pending)

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.verificacao.lazy.tsx', 'w') as f:
    f.write(content)

import { Lock } from 'lucide-react'

/** Campo apenas informativo para valores que a API calcula ao salvar. */
export function LockedValue({ label, hint }: { label: string; hint: string }) {
  return (
    <div className="flex flex-col gap-2">
      <span className="text-sm font-medium text-muted-foreground">{label}</span>
      <div className="flex h-8 items-center gap-2 rounded-lg border border-dashed bg-muted-subtle px-2.5 text-sm text-muted-foreground">
        <Lock className="size-3.5" aria-hidden />
        Após salvar
      </div>
      <span className="text-xs text-muted-foreground">{hint}</span>
    </div>
  )
}

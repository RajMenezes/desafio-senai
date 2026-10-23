import { Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Input } from '@/components/ui/input'

const SEARCH_DELAY_MS = 300

interface SearchInputProps {
  value: string
  onChange: (value: string) => void
  placeholder: string
}

/** Campo de busca que só avisa a mudança depois de uma pausa na digitação. */
export function SearchInput({ value, onChange, placeholder }: SearchInputProps) {
  const [text, setText] = useState(value)
  const [syncedValue, setSyncedValue] = useState(value)

  if (value !== syncedValue) {
    setSyncedValue(value)
    setText(value)
  }

  useEffect(() => {
    if (text === syncedValue) return
    const timer = setTimeout(() => onChange(text), SEARCH_DELAY_MS)
    return () => clearTimeout(timer)
  }, [text, syncedValue, onChange])

  return (
    <div className="relative min-w-40 flex-1">
      <Search
        className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground"
        aria-hidden
      />
      <Input
        type="search"
        value={text}
        onChange={(event) => setText(event.target.value)}
        placeholder={placeholder}
        aria-label={placeholder}
        maxLength={100}
        className="h-9 pl-9"
      />
    </div>
  )
}

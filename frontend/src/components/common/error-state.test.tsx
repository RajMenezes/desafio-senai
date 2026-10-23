import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '@/lib/api/errors'
import { ErrorState } from './error-state'

describe('ErrorState', () => {
  it('mostra o message da API e permite tentar de novo', async () => {
    const onRetry = vi.fn()
    render(
      <ErrorState
        title="Não foi possível carregar os jogadores"
        error={new ApiError(400, 'O filtro "só livres" não combina com o filtro por clube.')}
        onRetry={onRetry}
      />,
    )

    expect(screen.getByRole('alert')).toHaveTextContent('Não foi possível carregar os jogadores')
    expect(
      screen.getByText('O filtro "só livres" não combina com o filtro por clube.'),
    ).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))

    expect(onRetry).toHaveBeenCalledOnce()
  })

  it('desabilita o botão enquanto tenta de novo', () => {
    render(<ErrorState title="Erro" error={new Error()} onRetry={vi.fn()} retrying />)

    expect(screen.getByRole('button', { name: /Tentar de novo/ })).toBeDisabled()
  })
})

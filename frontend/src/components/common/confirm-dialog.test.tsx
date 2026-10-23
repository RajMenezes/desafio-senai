import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '@/lib/api/errors'
import { ConfirmDialog } from './confirm-dialog'

const baseProps = {
  open: true,
  onOpenChange: vi.fn(),
  title: 'Excluir jogador',
  description: 'O jogador sai da lista.',
  confirmLabel: 'Excluir jogador',
  onConfirm: vi.fn(),
  pending: false,
}

describe('ConfirmDialog', () => {
  it('confirma a ação', async () => {
    const onConfirm = vi.fn()
    render(<ConfirmDialog {...baseProps} onConfirm={onConfirm} />)

    await userEvent.click(screen.getByRole('button', { name: 'Excluir jogador' }))

    expect(onConfirm).toHaveBeenCalledOnce()
  })

  it('bloqueia os botões e não fecha enquanto a ação está em andamento', async () => {
    const onOpenChange = vi.fn()
    render(<ConfirmDialog {...baseProps} onOpenChange={onOpenChange} pending />)

    expect(screen.getByRole('button', { name: /Excluir jogador/ })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeDisabled()

    await userEvent.keyboard('{Escape}')

    expect(onOpenChange).not.toHaveBeenCalled()
  })

  it('mostra o message da API quando a ação é recusada', () => {
    render(
      <ConfirmDialog
        {...baseProps}
        error={new ApiError(409, 'Só jogadores livres podem ser excluídos.')}
        errorTitle="Exclusão recusada"
      />,
    )

    expect(screen.getByText('Exclusão recusada')).toBeInTheDocument()
    expect(screen.getByText('Só jogadores livres podem ser excluídos.')).toBeInTheDocument()
  })
})

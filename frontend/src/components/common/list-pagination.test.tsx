import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ListPagination } from './list-pagination'

function pageButtons() {
  return within(screen.getByRole('navigation', { name: 'Paginação' }))
    .getAllByRole('button', { name: /^Página \d+$/ })
    .map((button) => button.textContent)
}

describe('ListPagination', () => {
  it('mostra o intervalo, o total e a página atual', () => {
    render(
      <ListPagination
        meta={{ page: 3, limit: 10, total_items: 23, total_pages: 3 }}
        itemLabel="jogadores"
        onPageChange={vi.fn()}
      />,
    )

    expect(screen.getByText('Mostrando 21–23 de 23 jogadores')).toBeInTheDocument()
    expect(pageButtons()).toEqual(['1', '2', '3'])
    expect(screen.getByRole('button', { name: 'Página 3' })).toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('button', { name: 'Próxima página' })).toBeDisabled()
  })

  it('resume muitas páginas em torno da atual', () => {
    render(
      <ListPagination
        meta={{ page: 6, limit: 10, total_items: 200, total_pages: 20 }}
        itemLabel="jogadores"
        onPageChange={vi.fn()}
      />,
    )

    expect(pageButtons()).toEqual(['1', '5', '6', '7', '20'])
  })

  it('avisa a página escolhida', async () => {
    const onPageChange = vi.fn()
    render(
      <ListPagination
        meta={{ page: 1, limit: 10, total_items: 23, total_pages: 3 }}
        itemLabel="jogadores"
        onPageChange={onPageChange}
      />,
    )

    expect(screen.getByRole('button', { name: 'Página anterior' })).toBeDisabled()
    await userEvent.click(screen.getByRole('button', { name: 'Próxima página' }))
    await userEvent.click(screen.getByRole('button', { name: 'Página 3' }))

    expect(onPageChange.mock.calls).toEqual([[2], [3]])
  })
})

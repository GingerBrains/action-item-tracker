import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ConfirmProvider, useConfirm } from './ConfirmDialog'

function Trigger({ onResolve, options }) {
  const confirm = useConfirm()
  return (
    <button
      type="button"
      onClick={async () => {
        const result = await confirm(options)
        onResolve(result)
      }}
    >
      open
    </button>
  )
}

describe('ConfirmProvider', () => {
  it('resolves true when the confirm button is clicked', async () => {
    const user = userEvent.setup()
    let resolved
    render(
      <ConfirmProvider>
        <Trigger onResolve={(r) => (resolved = r)} options={{ title: 'Delete this?', confirmText: 'Delete' }} />
      </ConfirmProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'open' }))
    expect(await screen.findByText('Delete this?')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Delete' }))

    expect(resolved).toBe(true)
    expect(screen.queryByText('Delete this?')).not.toBeInTheDocument()
  })

  it('resolves false when the cancel button is clicked', async () => {
    const user = userEvent.setup()
    let resolved
    render(
      <ConfirmProvider>
        <Trigger onResolve={(r) => (resolved = r)} options={{ title: 'Delete this?' }} />
      </ConfirmProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'open' }))
    await screen.findByText('Delete this?')
    await user.click(screen.getByRole('button', { name: 'Cancel' }))

    expect(resolved).toBe(false)
  })

  it('resolves false when Escape is pressed', async () => {
    const user = userEvent.setup()
    let resolved
    render(
      <ConfirmProvider>
        <Trigger onResolve={(r) => (resolved = r)} options={{ title: 'Delete this?' }} />
      </ConfirmProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'open' }))
    await screen.findByText('Delete this?')
    await user.keyboard('{Escape}')

    expect(resolved).toBe(false)
  })

  it('useConfirm throws outside a provider', () => {
    function NoProvider() {
      useConfirm()
      return null
    }
    expect(() => render(<NoProvider />)).toThrow(/ConfirmProvider/)
  })
})

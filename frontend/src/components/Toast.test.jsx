import { describe, expect, it } from 'vitest'
import { render, screen, act, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ToastProvider, useToast } from './Toast'

function TestHarness({ onReady }) {
  const toast = useToast()
  onReady?.(toast)
  return null
}

function Wrapper({ children, ...props }) {
  return <ToastProvider {...props}>{children}</ToastProvider>
}

describe('ToastProvider', () => {
  it('renders a success toast and auto-dismisses after the configured duration', async () => {
    let api
    render(
      <Wrapper defaultDurationMs={50}>
        <TestHarness onReady={(t) => (api = t)} />
      </Wrapper>,
    )

    act(() => {
      api.success('Saved')
    })

    expect(await screen.findByText('Saved')).toBeInTheDocument()
    expect(screen.getByRole('region', { name: 'Notifications' })).toBeInTheDocument()

    await waitFor(() => expect(screen.queryByText('Saved')).not.toBeInTheDocument())
  })

  it('error toast can be dismissed manually', async () => {
    let api
    const user = userEvent.setup()
    render(
      <Wrapper defaultDurationMs={0}>
        <TestHarness onReady={(t) => (api = t)} />
      </Wrapper>,
    )

    act(() => {
      api.error('Bad thing')
    })

    expect(await screen.findByText('Bad thing')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Dismiss notification' }))
    expect(screen.queryByText('Bad thing')).not.toBeInTheDocument()
  })

  it('useToast throws outside a provider', () => {
    expect(() => render(<TestHarness />)).toThrow(/ToastProvider/)
  })
})

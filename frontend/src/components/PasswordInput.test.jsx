import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PasswordInput from './PasswordInput'

describe('PasswordInput', () => {
  it('renders as type=password by default', () => {
    render(<PasswordInput id="pw" defaultValue="secret" />)
    expect(screen.getByDisplayValue('secret')).toHaveAttribute('type', 'password')
  })

  it('toggles to type=text when the show button is clicked', async () => {
    const user = userEvent.setup()
    render(<PasswordInput id="pw" defaultValue="secret" />)

    const input = screen.getByDisplayValue('secret')
    expect(input).toHaveAttribute('type', 'password')

    await user.click(screen.getByRole('button', { name: 'Show password' }))
    expect(input).toHaveAttribute('type', 'text')

    await user.click(screen.getByRole('button', { name: 'Hide password' }))
    expect(input).toHaveAttribute('type', 'password')
  })

  it('forwards extra props to the input', () => {
    render(<PasswordInput id="pw" name="password" required placeholder="••••" />)
    const input = screen.getByPlaceholderText('••••')
    expect(input).toHaveAttribute('name', 'password')
    expect(input).toBeRequired()
  })
})

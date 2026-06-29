import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { AuthProvider, useAuth } from './AuthContext'

const BASE = 'http://test-api'

function jsonResponse(status, body) {
  return {
    status,
    ok: status >= 200 && status < 300,
    json: async () => body,
  }
}

function noBodyResponse(status) {
  return { status, ok: status >= 200 && status < 300, json: async () => ({}) }
}

function renderAuth() {
  return renderHook(() => useAuth(), { wrapper: AuthProvider })
}

beforeEach(() => {
  globalThis.fetch = vi.fn()
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('AuthProvider — initial mount', () => {
  it('refreshes on mount, hydrates token + user, sets isAdmin from role', async () => {
    fetch
      .mockResolvedValueOnce(jsonResponse(200, { accessToken: 'tok' }))
      .mockResolvedValueOnce(jsonResponse(200, { email: 'admin@example.com', role: 'ADMIN' }))

    const { result } = renderAuth()

    await waitFor(() => expect(result.current.initializing).toBe(false))
    expect(result.current.token).toBe('tok')
    expect(result.current.userEmail).toBe('admin@example.com')
    expect(result.current.isAdmin).toBe(true)

    const refreshCall = fetch.mock.calls[0]
    expect(refreshCall[0]).toBe(`${BASE}/auth/refresh`)
    expect(refreshCall[1].method).toBe('POST')
    expect(refreshCall[1].credentials).toBe('include')
  })

  it('sets isAdmin to false for non-admin roles', async () => {
    fetch
      .mockResolvedValueOnce(jsonResponse(200, { accessToken: 'tok' }))
      .mockResolvedValueOnce(jsonResponse(200, { email: 'u@example.com', role: 'USER' }))

    const { result } = renderAuth()

    await waitFor(() => expect(result.current.initializing).toBe(false))
    expect(result.current.isAdmin).toBe(false)
  })

  it('finishes initializing without a token when refresh returns non-OK', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(401))

    const { result } = renderAuth()

    await waitFor(() => expect(result.current.initializing).toBe(false))
    expect(result.current.token).toBeNull()
    expect(result.current.userEmail).toBeNull()
    expect(result.current.isAdmin).toBeNull()
    expect(fetch).toHaveBeenCalledTimes(1)
  })

  it('swallows network errors during mount refresh', async () => {
    fetch.mockRejectedValueOnce(new TypeError('network down'))

    const { result } = renderAuth()

    await waitFor(() => expect(result.current.initializing).toBe(false))
    expect(result.current.token).toBeNull()
  })
})

describe('AuthProvider — login', () => {
  it('on success, sets token + user state and returns server payload', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(401))
    const { result } = renderAuth()
    await waitFor(() => expect(result.current.initializing).toBe(false))

    fetch
      .mockResolvedValueOnce(
        jsonResponse(200, { accessToken: 'new-tok', userId: 42 }),
      )
      .mockResolvedValueOnce(
        jsonResponse(200, { email: 'a@b.c', role: 'USER' }),
      )

    let payload
    await act(async () => {
      payload = await result.current.login('a@b.c', 'pw')
    })

    expect(payload).toEqual({ accessToken: 'new-tok', userId: 42 })
    expect(result.current.token).toBe('new-tok')
    expect(result.current.userEmail).toBe('a@b.c')
    expect(result.current.isAdmin).toBe(false)

    const loginCall = fetch.mock.calls[1]
    expect(loginCall[0]).toBe(`${BASE}/auth/login`)
    expect(loginCall[1].method).toBe('POST')
    expect(loginCall[1].body).toBe(JSON.stringify({ email: 'a@b.c', password: 'pw' }))
    expect(loginCall[1].credentials).toBe('include')
  })

  it('on failure, throws server payload and leaves state untouched', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(401))
    const { result } = renderAuth()
    await waitFor(() => expect(result.current.initializing).toBe(false))

    fetch.mockResolvedValueOnce(jsonResponse(400, { message: 'Invalid credentials' }))

    await expect(
      act(async () => {
        await result.current.login('a@b.c', 'wrong')
      }),
    ).rejects.toMatchObject({ message: 'Invalid credentials' })

    expect(result.current.token).toBeNull()
    expect(result.current.userEmail).toBeNull()
  })
})

describe('AuthProvider — refresh', () => {
  it('returns the new access token and updates state on success', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(401))
    const { result } = renderAuth()
    await waitFor(() => expect(result.current.initializing).toBe(false))

    fetch.mockResolvedValueOnce(jsonResponse(200, { accessToken: 'fresh-tok' }))

    let returned
    await act(async () => {
      returned = await result.current.refresh()
    })

    expect(returned).toBe('fresh-tok')
    expect(result.current.token).toBe('fresh-tok')
  })

  it('returns null on non-OK response without throwing', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(401))
    const { result } = renderAuth()
    await waitFor(() => expect(result.current.initializing).toBe(false))

    fetch.mockResolvedValueOnce(noBodyResponse(401))

    let returned
    await act(async () => {
      returned = await result.current.refresh()
    })

    expect(returned).toBeNull()
    expect(result.current.token).toBeNull()
  })

  it('returns null on network error', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(401))
    const { result } = renderAuth()
    await waitFor(() => expect(result.current.initializing).toBe(false))

    fetch.mockRejectedValueOnce(new TypeError('offline'))

    let returned
    await act(async () => {
      returned = await result.current.refresh()
    })

    expect(returned).toBeNull()
  })
})

describe('AuthProvider — logout', () => {
  it('clears state and calls the logout endpoint', async () => {
    fetch
      .mockResolvedValueOnce(jsonResponse(200, { accessToken: 'tok' }))
      .mockResolvedValueOnce(jsonResponse(200, { email: 'a@b.c', role: 'ADMIN' }))

    const { result } = renderAuth()
    await waitFor(() => expect(result.current.initializing).toBe(false))
    expect(result.current.token).toBe('tok')

    fetch.mockResolvedValueOnce(noBodyResponse(204))

    await act(async () => {
      await result.current.logout()
    })

    expect(result.current.token).toBeNull()
    expect(result.current.userEmail).toBeNull()
    expect(result.current.isAdmin).toBeNull()

    const logoutCall = fetch.mock.calls[2]
    expect(logoutCall[0]).toBe(`${BASE}/auth/logout`)
    expect(logoutCall[1].method).toBe('POST')
    expect(logoutCall[1].credentials).toBe('include')
  })

  it('still clears local state when the logout endpoint fails', async () => {
    fetch
      .mockResolvedValueOnce(jsonResponse(200, { accessToken: 'tok' }))
      .mockResolvedValueOnce(jsonResponse(200, { email: 'a@b.c', role: 'USER' }))

    const { result } = renderAuth()
    await waitFor(() => expect(result.current.initializing).toBe(false))

    fetch.mockRejectedValueOnce(new TypeError('network down'))

    await act(async () => {
      await result.current.logout()
    })

    expect(result.current.token).toBeNull()
  })
})

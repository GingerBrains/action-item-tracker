import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
import { createApiClient } from './client'

function jsonResponse(status, body) {
  return {
    status,
    ok: status >= 200 && status < 300,
    json: async () => body,
  }
}

function noBodyResponse(status) {
  return { status, ok: status >= 200 && status < 300 }
}

const BASE = 'http://test-api'

beforeEach(() => {
  globalThis.fetch = vi.fn()
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('createApiClient', () => {
  it('sends GET with Authorization header and returns parsed JSON', async () => {
    fetch.mockResolvedValueOnce(jsonResponse(200, { id: 1, title: 'hi' }))
    const api = createApiClient('access-token')

    const result = await api.get('/meetings')

    expect(result).toEqual({ id: 1, title: 'hi' })
    expect(fetch).toHaveBeenCalledTimes(1)
    const [url, opts] = fetch.mock.calls[0]
    expect(url).toBe(`${BASE}/meetings`)
    expect(opts.method).toBe('GET')
    expect(opts.headers.Authorization).toBe('Bearer access-token')
    expect(opts.credentials).toBe('include')
    expect(opts.body).toBeUndefined()
  })

  it('omits Authorization header when no token is provided', async () => {
    fetch.mockResolvedValueOnce(jsonResponse(200, {}))
    const api = createApiClient(null)

    await api.get('/public')

    expect(fetch.mock.calls[0][1].headers.Authorization).toBeUndefined()
  })

  it('sends POST body as JSON', async () => {
    fetch.mockResolvedValueOnce(jsonResponse(201, { id: 7 }))
    const api = createApiClient('t')

    await api.post('/action-items', { title: 'do thing' })

    const opts = fetch.mock.calls[0][1]
    expect(opts.method).toBe('POST')
    expect(opts.body).toBe(JSON.stringify({ title: 'do thing' }))
    expect(opts.headers['Content-Type']).toBe('application/json')
  })

  it('returns null for 204 No Content', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(204))
    const api = createApiClient('t')

    const result = await api.del('/action-items/1')

    expect(result).toBeNull()
  })

  it('on 401 refreshes the token and retries with the new token', async () => {
    fetch
      .mockResolvedValueOnce(noBodyResponse(401))
      .mockResolvedValueOnce(jsonResponse(200, { ok: true }))
    const onRefresh = vi.fn().mockResolvedValue('new-token')
    const onUnauthorized = vi.fn()
    const api = createApiClient('stale-token', onUnauthorized, onRefresh)

    const result = await api.get('/meetings')

    expect(result).toEqual({ ok: true })
    expect(onRefresh).toHaveBeenCalledTimes(1)
    expect(onUnauthorized).not.toHaveBeenCalled()
    expect(fetch).toHaveBeenCalledTimes(2)
    expect(fetch.mock.calls[1][1].headers.Authorization).toBe('Bearer new-token')
  })

  it('on 401 when refresh returns null, calls onUnauthorized and throws 401', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(401))
    const onRefresh = vi.fn().mockResolvedValue(null)
    const onUnauthorized = vi.fn()
    const api = createApiClient('stale', onUnauthorized, onRefresh)

    await expect(api.get('/meetings')).rejects.toMatchObject({ status: 401 })
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
    expect(fetch).toHaveBeenCalledTimes(1)
  })

  it('on 401 when retry also returns 401, calls onUnauthorized and throws 401', async () => {
    fetch
      .mockResolvedValueOnce(noBodyResponse(401))
      .mockResolvedValueOnce(noBodyResponse(401))
    const onRefresh = vi.fn().mockResolvedValue('new-token')
    const onUnauthorized = vi.fn()
    const api = createApiClient('stale', onUnauthorized, onRefresh)

    await expect(api.get('/meetings')).rejects.toMatchObject({ status: 401 })
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
    expect(fetch).toHaveBeenCalledTimes(2)
  })

  it('on 401 when no onRefresh is provided, calls onUnauthorized and throws 401', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(401))
    const onUnauthorized = vi.fn()
    const api = createApiClient('stale', onUnauthorized)

    await expect(api.get('/meetings')).rejects.toMatchObject({ status: 401 })
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
  })

  it('throws a permission error on 403', async () => {
    fetch.mockResolvedValueOnce(noBodyResponse(403))
    const api = createApiClient('t')

    await expect(api.get('/admin')).rejects.toMatchObject({
      status: 403,
      message: 'You do not have permission to perform this action.',
    })
  })

  it('throws with server-provided message on non-OK responses', async () => {
    fetch.mockResolvedValueOnce(jsonResponse(400, { message: 'Title is required', field: 'title' }))
    const api = createApiClient('t')

    await expect(api.post('/action-items', {})).rejects.toMatchObject({
      status: 400,
      message: 'Title is required',
      field: 'title',
    })
  })

  it('falls back to "Request failed" when error body has no message', async () => {
    fetch.mockResolvedValueOnce(jsonResponse(500, {}))
    const api = createApiClient('t')

    await expect(api.get('/meetings')).rejects.toMatchObject({
      status: 500,
      message: 'Request failed',
    })
  })
})

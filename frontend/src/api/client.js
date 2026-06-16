const BASE_URL = import.meta.env.VITE_API_BASE_URL

export function createApiClient(token, onUnauthorized, onForbidden) {
  async function request(method, path, body) {
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    }
    if (body !== undefined) {
      options.body = JSON.stringify(body)
    }

    const res = await fetch(`${BASE_URL}${path}`, options)

    if (res.status === 401) {
      onUnauthorized?.()
      const err = new Error('Session expired. Please log in again.')
      err.status = 401
      throw err
    }
    if (res.status === 403) {
      onForbidden?.()
      const err = new Error('You do not have permission to perform this action.')
      err.status = 403
      throw err
    }
    if (res.status === 204) return null

    const data = await res.json()
    if (!res.ok) {
      const err = Object.assign(new Error(data.message || 'Request failed'), data)
      err.status = res.status
      throw err
    }
    return data
  }

  return {
    get: (path) => request('GET', path),
    post: (path, body) => request('POST', path, body),
    put: (path, body) => request('PUT', path, body),
    del: (path) => request('DELETE', path),
  }
}

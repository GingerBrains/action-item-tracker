const BASE_URL = import.meta.env.VITE_API_BASE_URL

export function createApiClient(token, onUnauthorized, onRefresh) {
  function doFetch(tkn, method, path, body) {
    const options = {
      method,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(tkn ? { Authorization: `Bearer ${tkn}` } : {}),
      },
    }
    if (body !== undefined) options.body = JSON.stringify(body)
    return fetch(`${BASE_URL}${path}`, options)
  }

  async function request(method, path, body) {
    let res = await doFetch(token, method, path, body)

    if (res.status === 401) {
      const newToken = onRefresh ? await onRefresh() : null
      if (newToken) {
        res = await doFetch(newToken, method, path, body)
      }
      if (res.status === 401) {
        onUnauthorized?.()
        const err = new Error('Session expired. Please log in again.')
        err.status = 401
        throw err
      }
    }

    if (res.status === 403) {
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

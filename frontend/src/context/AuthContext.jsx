import { createContext, useContext, useState, useCallback, useEffect, useRef } from 'react'

const AuthContext = createContext(null)

const BASE_URL = import.meta.env.VITE_API_BASE_URL

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null)
  const [isAdmin, setIsAdmin] = useState(null)
  const [userEmail, setUserEmail] = useState(null)
  const [initializing, setInitializing] = useState(true)
  const didInitRefresh = useRef(false)

  async function applyAccessToken(accessToken) {
    setToken(accessToken)
    const meRes = await fetch(`${BASE_URL}/auth/me`, {
      headers: { Authorization: `Bearer ${accessToken}` },
    })
    if (meRes.ok) {
      const me = await meRes.json()
      setUserEmail(me.email)
      setIsAdmin(me.role === 'ADMIN')
    }
  }

  useEffect(() => {
    if (didInitRefresh.current) return
    didInitRefresh.current = true

    fetch(`${BASE_URL}/auth/refresh`, {
      method: 'POST',
      credentials: 'include',
    })
      .then(async (res) => {
        if (res.ok) {
          const data = await res.json()
          await applyAccessToken(data.accessToken)
        }
      })
      .catch(() => {})
      .finally(() => setInitializing(false))
  }, [])

  const login = useCallback(async (email, password) => {
    const res = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ email, password }),
    })
    const data = await res.json()
    if (!res.ok) throw data
    await applyAccessToken(data.accessToken)
    return data
  }, [])

  const refresh = useCallback(async () => {
    try {
      const res = await fetch(`${BASE_URL}/auth/refresh`, {
        method: 'POST',
        credentials: 'include',
      })
      if (!res.ok) return null
      const data = await res.json()
      setToken(data.accessToken)
      return data.accessToken
    } catch {
      return null
    }
  }, [])

  const logout = useCallback(async () => {
    setToken(null)
    setIsAdmin(null)
    setUserEmail(null)
    try {
      await fetch(`${BASE_URL}/auth/logout`, {
        method: 'POST',
        credentials: 'include',
      })
    } catch {
      // best-effort
    }
  }, [])

  return (
    <AuthContext.Provider value={{ token, isAdmin, userEmail, initializing, login, logout, refresh }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}

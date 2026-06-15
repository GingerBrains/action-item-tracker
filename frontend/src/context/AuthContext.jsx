import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

const BASE_URL = import.meta.env.VITE_API_BASE_URL

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null)
  const [isAdmin, setIsAdmin] = useState(null) // null = unknown, true = admin, false = member
  const [userEmail, setUserEmail] = useState(null)

  const login = useCallback(async (email, password) => {
    const res = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })
    const data = await res.json()
    if (!res.ok) throw data

    setToken(data.token)
    setUserEmail(email)

    // Determine admin status: POST with empty body returns 403 for MEMBER, 400 for ADMIN
    const probe = await fetch(`${BASE_URL}/meetings`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${data.token}`,
      },
      body: JSON.stringify({}),
    })
    setIsAdmin(probe.status !== 403)

    return data
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    setIsAdmin(null)
    setUserEmail(null)
  }, [])

  const markNotAdmin = useCallback(() => {
    setIsAdmin(false)
  }, [])

  return (
    <AuthContext.Provider value={{ token, isAdmin, userEmail, login, logout, markNotAdmin }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}

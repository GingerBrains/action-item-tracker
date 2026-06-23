import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { createApiClient } from './client'

export function useApi() {
  const { token, logout, refresh } = useAuth()
  const navigate = useNavigate()

  const onUnauthorized = useCallback(() => {
    logout()
    navigate('/login')
  }, [logout, navigate])

  return createApiClient(token, onUnauthorized, refresh)
}

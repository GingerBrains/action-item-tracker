import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function PrivateRoute() {
  const { token, initializing } = useAuth()
  if (initializing) return null
  return token ? <Outlet /> : <Navigate to="/login" replace />
}

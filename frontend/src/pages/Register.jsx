import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'

const BASE_URL = import.meta.env.VITE_API_BASE_URL

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', password: '' })
  const [errors, setErrors] = useState({})
  const [globalError, setGlobalError] = useState(null)
  const [loading, setLoading] = useState(false)

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: undefined })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setGlobalError(null)
    setErrors({})
    setLoading(true)
    try {
      const res = await fetch(`${BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      const data = await res.json()
      if (!res.ok) {
        if (res.status === 400 && data.fields) {
          setErrors(data.fields)
        } else if (res.status === 409) {
          setGlobalError(data.message || 'An account with this email already exists.')
        } else {
          setGlobalError(data.message || 'Registration failed.')
        }
        return
      }
      navigate('/login')
    } catch {
      setGlobalError('Network error. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1 className="auth-logo">AIT</h1>
          <h2 className="auth-title">Action Item Tracker</h2>
          <p className="auth-subtitle">Create your account</p>
        </div>
        <form onSubmit={handleSubmit} className="auth-form">
          {globalError && <div className="alert alert-error">{globalError}</div>}
          <div className="form-group">
            <label className="form-label" htmlFor="fullName">Full name</label>
            <input
              id="fullName"
              name="fullName"
              type="text"
              required
              autoFocus
              className={`form-input${errors.fullName ? ' input-error' : ''}`}
              value={form.fullName}
              onChange={handleChange}
              placeholder="Jane Smith"
            />
            {errors.fullName && <p className="field-error">{errors.fullName}</p>}
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="email">Email address</label>
            <input
              id="email"
              name="email"
              type="email"
              required
              className={`form-input${errors.email ? ' input-error' : ''}`}
              value={form.email}
              onChange={handleChange}
              placeholder="you@example.com"
            />
            {errors.email && <p className="field-error">{errors.email}</p>}
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              required
              className={`form-input${errors.password ? ' input-error' : ''}`}
              value={form.password}
              onChange={handleChange}
              placeholder="••••••••"
            />
            {errors.password && <p className="field-error">{errors.password}</p>}
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>
        <p className="auth-switch">
          Already have an account?{' '}
          <Link to="/login" className="link">Sign in</Link>
        </p>
      </div>
    </div>
  )
}

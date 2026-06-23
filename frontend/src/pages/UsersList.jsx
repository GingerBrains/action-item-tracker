import { useState } from 'react'
import { useApi } from '../api/useApi'

export default function UsersList() {
  const api = useApi()
  const [users, setUsers] = useState([])
  const [idInput, setIdInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [pending, setPending] = useState({})
  const [saving, setSaving] = useState({})
  const [rowError, setRowError] = useState({})

  async function handleFindById(e) {
    e.preventDefault()
    const id = idInput.trim()
    if (!id) return
    setError(null)
    setLoading(true)
    try {
      const user = await api.get(`/users/${id}`)
      setUsers([user])
    } catch (err) {
      setError(err.message)
      setUsers([])
    } finally {
      setLoading(false)
    }
  }

  async function handleLoadAll() {
    setError(null)
    setLoading(true)
    try {
      const all = await api.get('/users')
      setUsers(all)
    } catch (err) {
      setError(err.message)
      setUsers([])
    } finally {
      setLoading(false)
    }
  }

  function handleRoleChange(userId, newRole) {
    setPending((prev) => ({ ...prev, [userId]: newRole }))
    setRowError((prev) => ({ ...prev, [userId]: undefined }))
  }

  async function handleSave(userId) {
    setSaving((prev) => ({ ...prev, [userId]: true }))
    setRowError((prev) => ({ ...prev, [userId]: undefined }))
    try {
      const updated = await api.put(`/users/${userId}/role`, { role: pending[userId] })
      setUsers((prev) => prev.map((u) => (u.id === userId ? updated : u)))
      setPending((prev) => { const next = { ...prev }; delete next[userId]; return next })
    } catch (err) {
      setRowError((prev) => ({ ...prev, [userId]: err.message }))
    } finally {
      setSaving((prev) => ({ ...prev, [userId]: false }))
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Users</h1>
      </div>

      <div className="card">
        <div className="card-header">
          <form onSubmit={handleFindById} style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <input
              type="number"
              className="form-input"
              placeholder="Find by user ID"
              value={idInput}
              onChange={(e) => setIdInput(e.target.value)}
              style={{ width: '180px' }}
            />
            <button type="submit" className="btn btn-primary btn-sm" disabled={loading || !idInput.trim()}>
              Find
            </button>
          </form>
          <button className="btn btn-primary btn-sm" onClick={handleLoadAll} disabled={loading}>
            {loading ? 'Loading...' : 'Load all users'}
          </button>
        </div>

        {error && <div className="alert alert-error" style={{ margin: '12px 0 0' }}>{error}</div>}

        {users.length > 0 && (
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => {
                const currentRole = pending[user.id] ?? user.role
                const isDirty = pending[user.id] !== undefined && pending[user.id] !== user.role
                return (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.fullName}</td>
                    <td>{user.email}</td>
                    <td>
                      <select
                        className="form-input"
                        value={currentRole}
                        onChange={(e) => handleRoleChange(user.id, e.target.value)}
                      >
                        <option value="MEMBER">MEMBER</option>
                        <option value="ADMIN">ADMIN</option>
                      </select>
                      {rowError[user.id] && <p className="field-error">{rowError[user.id]}</p>}
                    </td>
                    <td>
                      {isDirty && (
                        <button
                          className="btn btn-primary btn-sm"
                          disabled={saving[user.id]}
                          onClick={() => handleSave(user.id)}
                        >
                          {saving[user.id] ? 'Saving...' : 'Save'}
                        </button>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}

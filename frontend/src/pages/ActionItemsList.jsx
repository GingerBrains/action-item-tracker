import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useApi } from '../api/useApi'
import { useAuth } from '../context/AuthContext'
import { StatusBadge, PriorityBadge } from '../components/Badge'

const STATUSES = ['', 'OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
const PRIORITIES = ['', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

function isOverdue(item) {
  if (!item.dueDate) return false
  if (item.status === 'COMPLETED' || item.status === 'CANCELLED') return false
  return new Date(item.dueDate) < new Date(new Date().toDateString())
}

export default function ActionItemsList() {
  const api = useApi()
  const { isAdmin } = useAuth()
  const navigate = useNavigate()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [filterStatus, setFilterStatus] = useState('')
  const [filterPriority, setFilterPriority] = useState('')

  useEffect(() => {
    api.get('/action-items')
      .then(setItems)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  async function handleDelete(id) {
    if (!window.confirm('Delete this action item?')) return
    try {
      await api.del(`/action-items/${id}`)
      setItems((prev) => prev.filter((i) => i.id !== id))
    } catch (err) {
      alert(err.message)
    }
  }

  const filtered = items.filter((i) => {
    if (filterStatus && i.status !== filterStatus) return false
    if (filterPriority && i.priority !== filterPriority) return false
    return true
  })

  if (loading) return <div className="page-loading">Loading...</div>
  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Action Items</h1>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => navigate('/action-items/new')}>
            + New Action Item
          </button>
        )}
      </div>

      <div className="filter-bar">
        <div className="filter-group">
          <label className="filter-label">Status</label>
          <select
            className="form-input form-input-sm"
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
          >
            {STATUSES.map((s) => (
              <option key={s} value={s}>{s || 'All statuses'}</option>
            ))}
          </select>
        </div>
        <div className="filter-group">
          <label className="filter-label">Priority</label>
          <select
            className="form-input form-input-sm"
            value={filterPriority}
            onChange={(e) => setFilterPriority(e.target.value)}
          >
            {PRIORITIES.map((p) => (
              <option key={p} value={p}>{p || 'All priorities'}</option>
            ))}
          </select>
        </div>
        <span className="filter-count">{filtered.length} item{filtered.length !== 1 ? 's' : ''}</span>
      </div>

      {filtered.length === 0 ? (
        <div className="card">
          <p className="empty-state">
            {items.length === 0
              ? isAdmin
                ? 'No action items yet — create one to get started.'
                : 'No action items have been assigned.'
              : 'No items match the current filters.'}
          </p>
        </div>
      ) : (
        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Meeting</th>
                <th>Assignee</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Due Date</th>
                {isAdmin && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {filtered.map((item) => (
                <tr key={item.id} className={isOverdue(item) ? 'row-overdue' : ''}>
                  <td>{item.title}</td>
                  <td>
                    {item.meetingId
                      ? <Link to={`/meetings/${item.meetingId}`} className="link">#{item.meetingId}</Link>
                      : <span className="text-muted">—</span>}
                  </td>
                  <td>{item.assigneeName || <span className="text-muted">Unassigned</span>}</td>
                  <td><PriorityBadge value={item.priority} /></td>
                  <td><StatusBadge value={item.status} /></td>
                  <td className={isOverdue(item) ? 'text-overdue' : ''}>{item.dueDate || '—'}</td>
                  {isAdmin && (
                    <td>
                      <div className="action-group">
                        <button
                          className="btn btn-sm btn-secondary"
                          onClick={() => navigate(`/action-items/${item.id}/edit`)}
                        >
                          Edit
                        </button>
                        <button
                          className="btn btn-sm btn-danger"
                          onClick={() => handleDelete(item.id)}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

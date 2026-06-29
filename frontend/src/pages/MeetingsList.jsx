import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useApi } from '../api/useApi'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/Toast'
import { useConfirm } from '../components/ConfirmDialog'
import { StatusBadge } from '../components/Badge'

export default function MeetingsList() {
  const api = useApi()
  const { isAdmin } = useAuth()
  const toast = useToast()
  const confirm = useConfirm()
  const navigate = useNavigate()
  const [meetings, setMeetings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.get('/meetings')
      .then(setMeetings)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  async function handleDelete(id) {
    const ok = await confirm({
      title: 'Delete this meeting?',
      message: 'This cannot be undone.',
      confirmText: 'Delete',
      danger: true,
    })
    if (!ok) return
    try {
      await api.del(`/meetings/${id}`)
      setMeetings((prev) => prev.filter((m) => m.id !== id))
      toast.success('Meeting deleted')
    } catch (err) {
      toast.error(err.message)
    }
  }

  if (loading) return <div className="page-loading">Loading...</div>
  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Meetings</h1>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => navigate('/meetings/new')}>
            + New Meeting
          </button>
        )}
      </div>

      {meetings.length === 0 ? (
        <div className="card">
          <p className="empty-state">
            No meetings scheduled —{' '}
            {isAdmin ? <Link to="/meetings/new" className="link">create one to get started</Link> : 'check back later.'}
          </p>
        </div>
      ) : (
        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Description</th>
                <th>Date</th>
                <th>Status</th>
                {isAdmin && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {meetings.map((m) => (
                <tr key={m.id}>
                  <td>
                    <Link to={`/meetings/${m.id}`} className="link">{m.title}</Link>
                  </td>
                  <td className="text-muted">{m.description || '—'}</td>
                  <td>{m.meetingDate}</td>
                  <td><StatusBadge value={m.status} /></td>
                  {isAdmin && (
                    <td>
                      <div className="action-group">
                        <button
                          className="btn btn-sm btn-secondary"
                          onClick={() => navigate(`/meetings/${m.id}/edit`)}
                        >
                          Edit
                        </button>
                        <button
                          className="btn btn-sm btn-danger"
                          onClick={() => handleDelete(m.id)}
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

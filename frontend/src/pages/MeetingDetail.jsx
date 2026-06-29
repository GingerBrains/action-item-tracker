import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useApi } from '../api/useApi'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/Toast'
import { useConfirm } from '../components/ConfirmDialog'
import { StatusBadge, PriorityBadge } from '../components/Badge'

export default function MeetingDetail() {
  const { id } = useParams()
  const api = useApi()
  const { isAdmin } = useAuth()
  const toast = useToast()
  const confirm = useConfirm()
  const navigate = useNavigate()
  const [meeting, setMeeting] = useState(null)
  const [actionItems, setActionItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([api.get(`/meetings/${id}`), api.get('/action-items')])
      .then(([m, items]) => {
        setMeeting(m)
        setActionItems(items.filter((i) => i.meetingId === m.id))
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  async function handleDeleteMeeting() {
    const ok = await confirm({
      title: 'Delete this meeting?',
      message: 'All action items linked to this meeting will also be deleted. This cannot be undone.',
      confirmText: 'Delete',
      danger: true,
    })
    if (!ok) return
    try {
      await api.del(`/meetings/${id}`)
      toast.success('Meeting deleted')
      navigate('/meetings')
    } catch (err) {
      toast.error(err.message)
    }
  }

  async function handleDeleteItem(itemId) {
    const ok = await confirm({
      title: 'Delete this action item?',
      confirmText: 'Delete',
      danger: true,
    })
    if (!ok) return
    try {
      await api.del(`/action-items/${itemId}`)
      setActionItems((prev) => prev.filter((i) => i.id !== itemId))
      toast.success('Action item deleted')
    } catch (err) {
      toast.error(err.message)
    }
  }

  if (loading) return <div className="page-loading">Loading...</div>
  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>
  if (!meeting) return null

  return (
    <div className="page">
      <div className="breadcrumb">
        <Link to="/meetings" className="link">Meetings</Link>
        <span className="breadcrumb-sep">/</span>
        <span>{meeting.title}</span>
      </div>

      <div className="page-header">
        <h1 className="page-title">{meeting.title}</h1>
        {isAdmin && (
          <div className="action-group">
            <button className="btn btn-secondary" onClick={() => navigate(`/meetings/${id}/edit`)}>
              Edit
            </button>
            <button className="btn btn-danger" onClick={handleDeleteMeeting}>
              Delete
            </button>
          </div>
        )}
      </div>

      <div className="card meeting-meta">
        <div className="meta-row">
          <span className="meta-label">Date</span>
          <span>{meeting.meetingDate}</span>
        </div>
        <div className="meta-row">
          <span className="meta-label">Status</span>
          <StatusBadge value={meeting.status} />
        </div>
        {meeting.description && (
          <div className="meta-row">
            <span className="meta-label">Description</span>
            <span>{meeting.description}</span>
          </div>
        )}
        <div className="meta-row">
          <span className="meta-label">Created</span>
          <span>{new Date(meeting.createdAt).toLocaleDateString()}</span>
        </div>
      </div>

      <div className="section-header">
        <h2 className="section-title">Action Items</h2>
        {isAdmin && (
          <button
            className="btn btn-primary"
            onClick={() => navigate(`/action-items/new?meetingId=${id}`)}
          >
            + Add Action Item
          </button>
        )}
      </div>

      {actionItems.length === 0 ? (
        <div className="card">
          <p className="empty-state">
            No action items for this meeting —{' '}
            {isAdmin
              ? <button className="link-btn" onClick={() => navigate(`/action-items/new?meetingId=${id}`)}>add one now</button>
              : 'none have been assigned.'}
          </p>
        </div>
      ) : (
        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Assignee</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Due Date</th>
                {isAdmin && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {actionItems.map((item) => (
                <tr key={item.id}>
                  <td>{item.title}</td>
                  <td>{item.assigneeName || <span className="text-muted">Unassigned</span>}</td>
                  <td><PriorityBadge value={item.priority} /></td>
                  <td><StatusBadge value={item.status} /></td>
                  <td>{item.dueDate || '—'}</td>
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
                          onClick={() => handleDeleteItem(item.id)}
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

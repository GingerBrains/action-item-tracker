import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useApi } from '../api/useApi'
import { StatusBadge, PriorityBadge } from '../components/Badge'

function isOverdue(item) {
  if (!item.dueDate) return false
  if (item.status === 'COMPLETED' || item.status === 'CANCELLED') return false
  return new Date(item.dueDate) < new Date(new Date().toDateString())
}

function isDueSoon(item) {
  if (!item.dueDate) return false
  if (item.status === 'COMPLETED' || item.status === 'CANCELLED') return false
  const due = new Date(item.dueDate)
  const today = new Date(new Date().toDateString())
  const in7 = new Date(today)
  in7.setDate(in7.getDate() + 7)
  return due >= today && due <= in7
}

export default function Dashboard() {
  const api = useApi()
  const [meetings, setMeetings] = useState([])
  const [actionItems, setActionItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([api.get('/meetings'), api.get('/action-items')])
      .then(([m, a]) => {
        setMeetings(m)
        setActionItems(a)
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="page-loading">Loading...</div>
  if (error) return <div className="alert alert-error">{error}</div>

  const totalMeetings = meetings.length
  const openItems = actionItems.filter((i) => i.status === 'OPEN').length
  const overdueItems = actionItems.filter(isOverdue).length
  const completedItems = actionItems.filter((i) => i.status === 'COMPLETED').length
  const recentMeetings = [...meetings]
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .slice(0, 5)
  const dueSoon = actionItems.filter(isDueSoon).slice(0, 5)

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Dashboard</h1>
      </div>

      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-label">Total Meetings</div>
          <div className="stat-value">{totalMeetings}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Open Action Items</div>
          <div className="stat-value stat-open">{openItems}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Overdue Items</div>
          <div className="stat-value stat-overdue">{overdueItems}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Completed Items</div>
          <div className="stat-value stat-completed">{completedItems}</div>
        </div>
      </div>

      <div className="dashboard-grid">
        <section className="card">
          <div className="card-header">
            <h2 className="card-title">Recent Meetings</h2>
            <Link to="/meetings" className="card-link">View all</Link>
          </div>
          {recentMeetings.length === 0 ? (
            <p className="empty-state">No meetings scheduled — create one to get started.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {recentMeetings.map((m) => (
                  <tr key={m.id}>
                    <td>
                      <Link to={`/meetings/${m.id}`} className="link">{m.title}</Link>
                    </td>
                    <td>{m.meetingDate}</td>
                    <td><StatusBadge value={m.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <section className="card">
          <div className="card-header">
            <h2 className="card-title">Due This Week</h2>
            <Link to="/action-items" className="card-link">View all</Link>
          </div>
          {dueSoon.length === 0 ? (
            <p className="empty-state">No action items due in the next 7 days.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Assignee</th>
                  <th>Priority</th>
                  <th>Due</th>
                </tr>
              </thead>
              <tbody>
                {dueSoon.map((item) => (
                  <tr key={item.id}>
                    <td>{item.title}</td>
                    <td>{item.assigneeName || <span className="text-muted">Unassigned</span>}</td>
                    <td><PriorityBadge value={item.priority} /></td>
                    <td>{item.dueDate}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </div>
  )
}

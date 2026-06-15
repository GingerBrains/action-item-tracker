import { useState, useEffect } from 'react'
import { useParams, useNavigate, useSearchParams, Link } from 'react-router-dom'
import { useApi } from '../api/useApi'

const STATUSES = ['OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

const EMPTY = {
  title: '',
  description: '',
  dueDate: '',
  status: 'OPEN',
  priority: 'MEDIUM',
  meetingId: '',
  assigneeId: '',
}

export default function ActionItemForm() {
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const isEdit = Boolean(id)
  const api = useApi()
  const navigate = useNavigate()

  const [form, setForm] = useState({
    ...EMPTY,
    meetingId: searchParams.get('meetingId') || '',
  })
  const [meetings, setMeetings] = useState([])
  const [fieldErrors, setFieldErrors] = useState({})
  const [globalError, setGlobalError] = useState(null)
  const [loading, setLoading] = useState(false)
  const [fetching, setFetching] = useState(true)

  useEffect(() => {
    const tasks = [api.get('/meetings')]
    if (isEdit) tasks.push(api.get(`/action-items/${id}`))

    Promise.all(tasks)
      .then(([mList, item]) => {
        setMeetings(mList)
        if (item) {
          setForm({
            title: item.title,
            description: item.description || '',
            dueDate: item.dueDate || '',
            status: item.status,
            priority: item.priority,
            meetingId: item.meetingId ?? '',
            assigneeId: item.assigneeId ?? '',
          })
        }
      })
      .catch((err) => setGlobalError(err.message))
      .finally(() => setFetching(false))
  }, [id])

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
    setFieldErrors({ ...fieldErrors, [e.target.name]: undefined })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setGlobalError(null)
    setFieldErrors({})
    setLoading(true)

    const payload = {
      ...form,
      meetingId: form.meetingId ? Number(form.meetingId) : undefined,
      assigneeId: form.assigneeId ? Number(form.assigneeId) : undefined,
    }
    // Remove empty optional fields
    if (!payload.assigneeId) delete payload.assigneeId
    if (!payload.meetingId) delete payload.meetingId

    try {
      if (isEdit) {
        await api.put(`/action-items/${id}`, payload)
        navigate(-1)
      } else {
        await api.post('/action-items', payload)
        if (form.meetingId) {
          navigate(`/meetings/${form.meetingId}`)
        } else {
          navigate('/action-items')
        }
      }
    } catch (err) {
      if (err.status === 400 && err.errors) {
        setFieldErrors(err.errors)
      } else {
        setGlobalError(err.message)
      }
    } finally {
      setLoading(false)
    }
  }

  if (fetching) return <div className="page-loading">Loading...</div>

  return (
    <div className="page">
      <div className="breadcrumb">
        <Link to="/action-items" className="link">Action Items</Link>
        <span className="breadcrumb-sep">/</span>
        <span>{isEdit ? 'Edit Action Item' : 'New Action Item'}</span>
      </div>

      <div className="page-header">
        <h1 className="page-title">{isEdit ? 'Edit Action Item' : 'New Action Item'}</h1>
      </div>

      <div className="card form-card">
        <form onSubmit={handleSubmit}>
          {globalError && <div className="alert alert-error">{globalError}</div>}

          <div className="form-group">
            <label className="form-label" htmlFor="title">Title <span className="required">*</span></label>
            <input
              id="title"
              name="title"
              type="text"
              required
              className={`form-input${fieldErrors.title ? ' input-error' : ''}`}
              value={form.title}
              onChange={handleChange}
            />
            {fieldErrors.title && <p className="field-error">{fieldErrors.title}</p>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="description">Description</label>
            <textarea
              id="description"
              name="description"
              rows={3}
              className="form-input"
              value={form.description}
              onChange={handleChange}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="status">Status <span className="required">*</span></label>
              <select
                id="status"
                name="status"
                className="form-input"
                value={form.status}
                onChange={handleChange}
              >
                {STATUSES.map((s) => (
                  <option key={s} value={s}>{s.replace('_', ' ')}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="priority">Priority <span className="required">*</span></label>
              <select
                id="priority"
                name="priority"
                className="form-input"
                value={form.priority}
                onChange={handleChange}
              >
                {PRIORITIES.map((p) => (
                  <option key={p} value={p}>{p}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="dueDate">Due Date</label>
              <input
                id="dueDate"
                name="dueDate"
                type="date"
                className={`form-input${fieldErrors.dueDate ? ' input-error' : ''}`}
                value={form.dueDate}
                onChange={handleChange}
              />
              {fieldErrors.dueDate && <p className="field-error">{fieldErrors.dueDate}</p>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="meetingId">Meeting</label>
              <select
                id="meetingId"
                name="meetingId"
                className="form-input"
                value={form.meetingId}
                onChange={handleChange}
              >
                <option value="">— Select meeting —</option>
                {meetings.map((m) => (
                  <option key={m.id} value={m.id}>{m.title}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="assigneeId">Assignee ID <span className="form-hint">(optional — numeric user ID)</span></label>
            <input
              id="assigneeId"
              name="assigneeId"
              type="number"
              min={1}
              className={`form-input form-input-narrow${fieldErrors.assigneeId ? ' input-error' : ''}`}
              value={form.assigneeId}
              onChange={handleChange}
              placeholder="e.g. 2"
            />
            {fieldErrors.assigneeId && <p className="field-error">{fieldErrors.assigneeId}</p>}
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : isEdit ? 'Save Changes' : 'Create Action Item'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
